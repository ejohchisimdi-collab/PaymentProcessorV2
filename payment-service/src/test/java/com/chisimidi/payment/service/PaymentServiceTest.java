package com.chisimidi.payment.service;

import com.chisimdi.payment.events.AuthorizationEvent;
import com.chisimdi.payment.events.CaptureEvent;
import com.chisimdi.payment.events.FailAuthorizationEvent;
import com.chisimdi.payment.events.SettlementEvent;
import com.chisimidi.payment.service.exceptions.ConflictException;
import com.chisimidi.payment.service.exceptions.ResourceNotFoundException;
import com.chisimidi.payment.service.mappers.PaymentMapper;
import com.chisimidi.payment.service.models.Payment;
import com.chisimidi.payment.service.models.PaymentStatus;
import com.chisimidi.payment.service.repositories.PaymentIdempotencyRepository;
import com.chisimidi.payment.service.repositories.PaymentRepository;
import com.chisimidi.payment.service.services.FraudService;
import com.chisimidi.payment.service.services.PaymentService;
import com.chisimidi.payment.service.services.RestClientService;
import com.chisimidi.payment.service.utils.AccountType;
import com.chisimidi.payment.service.utils.CaptureType;
import com.chisimidi.payment.service.utils.FraudUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private RestClientService restClientService;
    @Mock
    private FraudService fraudService;
    @Mock
    private KafkaTemplate<String,Object> kafkaTemplate;
    @Mock
    private PaymentIdempotencyRepository paymentIdempotencyRepository;
    @InjectMocks
    private PaymentService paymentService;

    @Test
    void pendingProcessorTest(){
        int merchantId=1;
        String merchantAccountToken="abc";
        String customerAccountToken="efg";
        BigDecimal amount=BigDecimal.valueOf(2000);

        when(restClientService.doesMerchantAndAccountExist(merchantAccountToken,merchantId)).thenReturn(true);
        when(restClientService.doesCustomerAccountExist(customerAccountToken)).thenReturn(true);
        when(restClientService.getTransactionLimit(merchantId)).thenReturn(BigDecimal.valueOf(3000));
        when(restClientService.getMerchantCurrency(merchantId)).thenReturn("USD");
        when(restClientService.getCustomerAccountCurrency(customerAccountToken)).thenReturn("USD");
        when(restClientService.getCustomerAccountType(customerAccountToken)).thenReturn(AccountType.CREDIT);

        Payment payment=paymentService.pendingProcessor(merchantId,merchantAccountToken,customerAccountToken,amount);

        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CREDIT_PENDING);
        assertThat(payment.getAmountAfterConversion()).isEqualTo(payment.getAmount());
    }
    @Test
    void pendingProcessorTest_ConversionDifference(){
        int merchantId=1;
        String merchantAccountToken="abc";
        String customerAccountToken="efg";
        BigDecimal amount=BigDecimal.valueOf(2000);

        when(restClientService.doesMerchantAndAccountExist(merchantAccountToken,merchantId)).thenReturn(true);
        when(restClientService.doesCustomerAccountExist(customerAccountToken)).thenReturn(true);
        when(restClientService.getTransactionLimit(merchantId)).thenReturn(BigDecimal.valueOf(3000));
        when(restClientService.getMerchantCurrency(merchantId)).thenReturn("USD");
        when(restClientService.getCustomerAccountCurrency(customerAccountToken)).thenReturn("EUR");
        when(restClientService.getCustomerAccountType(customerAccountToken)).thenReturn(AccountType.CREDIT);

        Payment payment=paymentService.pendingProcessor(merchantId,merchantAccountToken,customerAccountToken,amount);

        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CREDIT_PENDING);
        assertThat(payment.getAmountAfterConversion()).isNotEqualTo(payment.getAmount());
    }
    @Test
    void pendingProcessorTest_BankPending(){
        int merchantId=1;
        String merchantAccountToken="abc";
        String customerAccountToken="efg";
        BigDecimal amount=BigDecimal.valueOf(2000);

        when(restClientService.doesMerchantAndAccountExist(merchantAccountToken,merchantId)).thenReturn(true);
        when(restClientService.doesCustomerAccountExist(customerAccountToken)).thenReturn(true);
        when(restClientService.getTransactionLimit(merchantId)).thenReturn(BigDecimal.valueOf(3000));
        when(restClientService.getMerchantCurrency(merchantId)).thenReturn("USD");
        when(restClientService.getCustomerAccountCurrency(customerAccountToken)).thenReturn("EUR");
        when(restClientService.getCustomerAccountType(customerAccountToken)).thenReturn(AccountType.BANK);

        Payment payment=paymentService.pendingProcessor(merchantId,merchantAccountToken,customerAccountToken,amount);

        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.BANK_PENDING);
        assertThat(payment.getAmountAfterConversion()).isNotEqualTo(payment.getAmount());
    }

    @Test
    void pendingProcessorTest_ThrowsResourceNotFoundExceptionForCustomerAccount(){
        int merchantId=1;
        String merchantAccountToken="abc";
        String customerAccountToken="efg";
        BigDecimal amount=BigDecimal.valueOf(2000);

        when(restClientService.doesMerchantAndAccountExist(merchantAccountToken,merchantId)).thenReturn(true);
        when(restClientService.doesCustomerAccountExist(customerAccountToken)).thenReturn(false);


        assertThatThrownBy(()->paymentService.pendingProcessor(merchantId,merchantAccountToken,customerAccountToken,amount)).isInstanceOf(ResourceNotFoundException.class);


    }
    @Test
    void pendingProcessorTest_ThrowsResourceNotFoundExceptionForMerchantAccount(){
        int merchantId=1;
        String merchantAccountToken="abc";
        String customerAccountToken="efg";
        BigDecimal amount=BigDecimal.valueOf(2000);

        when(restClientService.doesMerchantAndAccountExist(merchantAccountToken,merchantId)).thenReturn(false);



        assertThatThrownBy(()->paymentService.pendingProcessor(merchantId,merchantAccountToken,customerAccountToken,amount)).isInstanceOf(ResourceNotFoundException.class);


    }

    @Test
    void validateProcessor_HappyPath(){
        Payment payment=new Payment();
        payment.setConversionFee(BigDecimal.ZERO);
        payment.setPaymentStatus(PaymentStatus.CREDIT_PENDING);
        payment.setAmount(BigDecimal.valueOf(2000));

        FraudUtil fraudUtil=new FraudUtil();
        fraudUtil.setFraudScore(3);
        fraudUtil.setWarnings(new ArrayList<>());

        when(fraudService.calculateFraud(payment.getCustomerAccount(),payment.getAmount(),payment.getCurrency())).thenReturn(fraudUtil);
        when(restClientService.getCustomerBalance(payment.getCustomerAccount())).thenReturn(BigDecimal.valueOf(5000));

        Payment payment1=paymentService.validateProcessor(payment);

        assertThat(payment1.getPaymentStatus()).isEqualTo(PaymentStatus.VALIDATED);

    }
    @Test
    void validateProcessor_InsufficientFunds(){
        Payment payment=new Payment();
        payment.setConversionFee(BigDecimal.ZERO);
        payment.setPaymentStatus(PaymentStatus.CREDIT_PENDING);
        payment.setAmount(BigDecimal.valueOf(2000));

        FraudUtil fraudUtil=new FraudUtil();
        fraudUtil.setFraudScore(3);
        fraudUtil.setWarnings(new ArrayList<>());

        when(fraudService.calculateFraud(payment.getCustomerAccount(),payment.getAmount(),payment.getCurrency())).thenReturn(fraudUtil);
        when(restClientService.getCustomerBalance(payment.getCustomerAccount())).thenReturn(BigDecimal.valueOf(0));

        Payment payment1=paymentService.validateProcessor(payment);

        assertThat(payment1.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(payment1.getWarnings().get(0)).isEqualTo("Insufficient funds");

    }
    @Test
    void validateProcessor_FraudScore(){
        Payment payment=new Payment();
        payment.setConversionFee(BigDecimal.ZERO);
        payment.setPaymentStatus(PaymentStatus.CREDIT_PENDING);
        payment.setAmount(BigDecimal.valueOf(2000));

        FraudUtil fraudUtil=new FraudUtil();
        fraudUtil.setFraudScore(5);
        fraudUtil.setWarnings(new ArrayList<>());

        when(fraudService.calculateFraud(payment.getCustomerAccount(),payment.getAmount(),payment.getCurrency())).thenReturn(fraudUtil);
        when(restClientService.getCustomerBalance(payment.getCustomerAccount())).thenReturn(BigDecimal.valueOf(2000));

        Payment payment1=paymentService.validateProcessor(payment);

        assertThat(payment1.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);

    }

    @Test
    void authorisedProcessorTest(){
        Payment payment=new Payment();
        payment.setPaymentStatus(PaymentStatus.VALIDATED);
        payment.setAmount(BigDecimal.valueOf(2000));
        payment.setConversionFee(BigDecimal.valueOf(4));

        Payment payment1=paymentService.authorisedProcessor(payment);

        assertThat(payment.getAuthorizationDueDate().toLocalDate()).isEqualTo(LocalDateTime.now().plusDays(7).toLocalDate());

        verify(restClientService).reserveFunds(payment.getCustomerAccount(), payment.getAmount().add(payment.getConversionFee()));
        verify(kafkaTemplate).send(eq("authorization-completed"),any(AuthorizationEvent.class));
    }

    @Test
    void failPaymentsTest(){
        Payment payment=new Payment();
        List<Payment>payments=new ArrayList<>();
        ArgumentCaptor<List<Payment>>captor=ArgumentCaptor.forClass(List.class);

        when(paymentRepository.findByAuthorizationDueDateBeforeAndPaymentStatus(any(LocalDateTime.class),eq(PaymentStatus.AUTHORISED))).thenReturn(List.of(payment));
        when(paymentRepository.saveAll(any(List.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        paymentService.failPayments();

        verify(paymentRepository).saveAll(captor.capture());

        payments=captor.getValue();

        assertThat(payments.get(0).getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);

        verify(kafkaTemplate).send(eq("authorization-failed"),any(FailAuthorizationEvent.class));

    }

    @Test
    void capturePaymentTest_Automatic(){
        Payment payment=new Payment();
        payment.setPaymentStatus(PaymentStatus.AUTHORISED);
        when(restClientService.getCaptureType(payment.getMerchantId())).thenReturn(CaptureType.AUTOMATIC);

        Payment payment1=paymentService.capturePayments(payment);

        assertThat(payment1.getPaymentStatus()).isEqualTo(PaymentStatus.CAPTURED);

        verify(kafkaTemplate).send(eq("payment-captured"),any(CaptureEvent.class));


    }
    @Test
    void captureTestManual_HappyPath(){
        int paymentId=1;
        int merchantId=1;
        Payment payment=new Payment();
        payment.setAuthorizationDueDate(LocalDateTime.now().plusDays(1));

        when(paymentRepository.findByIdAndPaymentStatusAndMerchantId(paymentId,PaymentStatus.AUTHORISED,merchantId)).thenReturn(payment);

        Payment payment1=paymentService.manualCapturing(paymentId,merchantId);

        assertThat(payment1.getPaymentStatus()).isEqualTo(PaymentStatus.CAPTURED);

        verify(kafkaTemplate).send(eq("payment-captured"),any(CaptureEvent.class));
    }

    @Test
    void captureTestManual_ResourceNotFoundException(){
        int paymentId=1;
        int merchantId=1;
        Payment payment=new Payment();
        payment.setAuthorizationDueDate(LocalDateTime.now().plusDays(1));

        when(paymentRepository.findByIdAndPaymentStatusAndMerchantId(paymentId,PaymentStatus.AUTHORISED,merchantId)).thenReturn(null);

        assertThatThrownBy(()->paymentService.manualCapturing(paymentId,merchantId)).isInstanceOf(ResourceNotFoundException.class);



        verify(kafkaTemplate,never()).send(eq("payment-captured"),any(CaptureEvent.class));
    }
    @Test
    void captureTestManual_ConflictException(){
        int paymentId=1;
        int merchantId=1;
        Payment payment=new Payment();
        payment.setAuthorizationDueDate(LocalDateTime.now().minusDays(1));

        when(paymentRepository.findByIdAndPaymentStatusAndMerchantId(paymentId,PaymentStatus.AUTHORISED,merchantId)).thenReturn(payment);

        assertThatThrownBy(()->paymentService.manualCapturing(paymentId,merchantId)).isInstanceOf(ConflictException.class);



        verify(kafkaTemplate,never()).send(eq("payment-captured"),any(CaptureEvent.class));
    }

    @Test
    void settlePaymentTest(){
       Payment payment=new Payment();
       payment.setPaymentStatus(PaymentStatus.CAPTURED);
       payment.setAmountAfterConversion(BigDecimal.valueOf(2000));
       payment.setPlatformFee(BigDecimal.valueOf(2));

       Payment payment1=paymentService.settlePayment(payment);

       assertThat(payment1.getPaymentStatus()).isEqualTo(PaymentStatus.SETTLED);

       verify(kafkaTemplate).send(eq("payment-settled"),any(SettlementEvent.class));
    }

    @Test
    void settleBankPayments_HappyPath(){
        Payment payment=new Payment();
        payment.setPaymentStatus(PaymentStatus.BANK_PENDING);
        payment.setAmount(BigDecimal.valueOf(2000));
        payment.setConversionFee(BigDecimal.ZERO);
        payment.setAmountAfterConversion(BigDecimal.valueOf(2000));
        payment.setPlatformFee(BigDecimal.valueOf(2));
        ArgumentCaptor<List<Payment>>captor=ArgumentCaptor.forClass(List.class);
        List<Payment>payments=new ArrayList<>();

        FraudUtil fraudUtil=new FraudUtil();
        fraudUtil.setFraudScore(2);
        fraudUtil.setWarnings(new ArrayList<>());

        when(paymentRepository.findByPaymentStatusAndDone(PaymentStatus.BANK_PENDING,false)).thenReturn(List.of(payment));
        when(fraudService.calculateFraud(payment.getCustomerAccount(), payment.getAmount(), payment.getCurrency())).thenReturn(fraudUtil);
        when(restClientService.getCustomerBalance(payment.getCustomerAccount())).thenReturn(BigDecimal.valueOf(20000));
        when(paymentRepository.saveAll(any(List.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        paymentService.settleBankPayments();

        verify(paymentRepository).saveAll(captor.capture());

        payments=captor.getValue();

        assertThat(payments.get(0).getPaymentStatus()).isEqualTo(PaymentStatus.SETTLED);

        verify(kafkaTemplate).send(eq("payment-settled"),any(SettlementEvent.class));

    }

    @Test
    void settleBankPayments_InsufficientFundsTest(){
        Payment payment=new Payment();
        payment.setPaymentStatus(PaymentStatus.BANK_PENDING);
        payment.setAmount(BigDecimal.valueOf(2000));
        payment.setConversionFee(BigDecimal.ZERO);
        payment.setAmountAfterConversion(BigDecimal.valueOf(2000));
        payment.setPlatformFee(BigDecimal.valueOf(2));
        ArgumentCaptor<List<Payment>>captor=ArgumentCaptor.forClass(List.class);
        List<Payment>payments=new ArrayList<>();

        FraudUtil fraudUtil=new FraudUtil();
        fraudUtil.setFraudScore(2);
        fraudUtil.setWarnings(new ArrayList<>());

        when(paymentRepository.findByPaymentStatusAndDone(PaymentStatus.BANK_PENDING,false)).thenReturn(List.of(payment));
        when(fraudService.calculateFraud(payment.getCustomerAccount(), payment.getAmount(), payment.getCurrency())).thenReturn(fraudUtil);
        when(restClientService.getCustomerBalance(payment.getCustomerAccount())).thenReturn(BigDecimal.valueOf(0));
        when(paymentRepository.saveAll(any(List.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        paymentService.settleBankPayments();

        verify(paymentRepository).saveAll(captor.capture());

        payments=captor.getValue();

        assertThat(payments.get(0).getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);

        verify(kafkaTemplate,never()).send(eq("payment-settled"),any(SettlementEvent.class));

    }

    @Test
    void settleBankPayments_FraudScoreHigh(){
        Payment payment=new Payment();
        payment.setPaymentStatus(PaymentStatus.BANK_PENDING);
        payment.setAmount(BigDecimal.valueOf(2000));
        payment.setConversionFee(BigDecimal.ZERO);
        payment.setAmountAfterConversion(BigDecimal.valueOf(2000));
        payment.setPlatformFee(BigDecimal.valueOf(2));
        ArgumentCaptor<List<Payment>>captor=ArgumentCaptor.forClass(List.class);
        List<Payment>payments=new ArrayList<>();

        FraudUtil fraudUtil=new FraudUtil();
        fraudUtil.setFraudScore(6);
        fraudUtil.setWarnings(new ArrayList<>());

        when(paymentRepository.findByPaymentStatusAndDone(PaymentStatus.BANK_PENDING,false)).thenReturn(List.of(payment));
        when(fraudService.calculateFraud(payment.getCustomerAccount(), payment.getAmount(), payment.getCurrency())).thenReturn(fraudUtil);
        when(restClientService.getCustomerBalance(payment.getCustomerAccount())).thenReturn(BigDecimal.valueOf(20000));
        when(paymentRepository.saveAll(any(List.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        paymentService.settleBankPayments();

        verify(paymentRepository).saveAll(captor.capture());

        payments=captor.getValue();

        assertThat(payments.get(0).getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);

        verify(kafkaTemplate,never()).send(eq("payment-settled"),any(SettlementEvent.class));

    }




}
