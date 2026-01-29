package com.chisimidi.payment.service;

import com.chisimdi.payment.events.RefundCompletedEvent;
import com.chisimidi.payment.service.exceptions.ResourceNotFoundException;
import com.chisimidi.payment.service.mappers.RefundMapper;
import com.chisimidi.payment.service.models.Payment;
import com.chisimidi.payment.service.models.PaymentStatus;
import com.chisimidi.payment.service.models.RefundStatus;
import com.chisimidi.payment.service.models.Refunds;
import com.chisimidi.payment.service.repositories.PaymentRepository;
import com.chisimidi.payment.service.repositories.RefundIdempotencyRepository;
import com.chisimidi.payment.service.repositories.RefundsRepository;
import com.chisimidi.payment.service.services.RefundService;
import com.chisimidi.payment.service.services.RestClientService;
import com.chisimidi.payment.service.utils.RefundType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RefundServiceTest {
    @Mock
    private RestClientService restClientService;
    @Mock
    private RefundMapper refundMapper;
    @Mock
    private RefundsRepository refundsRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private KafkaTemplate<String,Object> kafkaTemplate;
    @Mock
    private RefundIdempotencyRepository refundIdempotencyRepository;
    @InjectMocks
    private RefundService refundService;

    @Test
    void convertToRefundTest(){
        int merchantId=1;
        int paymentId=1;
        BigDecimal amount=BigDecimal.valueOf(2000);

        Payment payment=new Payment();
        payment.setCurrency("USD");

        when(paymentRepository.findByIdAndPaymentStatus(paymentId, PaymentStatus.SETTLED)).thenReturn(payment);
        when(restClientService.doesMerchantSettingExist(merchantId)).thenReturn(true);
        when(restClientService.getMerchantRefundType(merchantId)).thenReturn(RefundType.COMPLETE);
        when(restClientService.getMerchantCurrency(merchantId)).thenReturn("USD");

        Refunds refunds=refundService.convertPaymentToRefund(merchantId,paymentId,amount);

        assertThat(refunds.getRefundType()).isEqualTo(RefundType.COMPLETE);
        assertThat(refunds.getAmountAfterConversion()).isEqualTo(refunds.getAmount());
    }

    @Test
    void convertToRefundTest_ConversionDifference(){
        int merchantId=1;
        int paymentId=1;
        BigDecimal amount=BigDecimal.valueOf(2000);

        Payment payment=new Payment();
        payment.setCurrency("EUR");

        when(paymentRepository.findByIdAndPaymentStatus(paymentId, PaymentStatus.SETTLED)).thenReturn(payment);
        when(restClientService.doesMerchantSettingExist(merchantId)).thenReturn(true);
        when(restClientService.getMerchantRefundType(merchantId)).thenReturn(RefundType.COMPLETE);
        when(restClientService.getMerchantCurrency(merchantId)).thenReturn("USD");

        Refunds refunds=refundService.convertPaymentToRefund(merchantId,paymentId,amount);

        assertThat(refunds.getRefundType()).isEqualTo(RefundType.COMPLETE);
        assertThat(refunds.getAmountAfterConversion()).isNotEqualTo(refunds.getAmount());
    }

    @Test
    void convertToRefundTest_MerchantSettingNotFound(){
        int merchantId=1;
        int paymentId=1;
        BigDecimal amount=BigDecimal.valueOf(2000);

        Payment payment=new Payment();
        payment.setCurrency("USD");

        when(paymentRepository.findByIdAndPaymentStatus(paymentId, PaymentStatus.SETTLED)).thenReturn(payment);
        when(restClientService.doesMerchantSettingExist(merchantId)).thenReturn(false);


        assertThatThrownBy(()->refundService.convertPaymentToRefund(merchantId,paymentId,amount)).isInstanceOf(ResourceNotFoundException.class);

    }

    @Test
    void convertToRefundTest_PaymentNotFound(){
        int merchantId=1;
        int paymentId=1;
        BigDecimal amount=BigDecimal.valueOf(2000);

        Payment payment=new Payment();
        payment.setCurrency("USD");

        when(paymentRepository.findByIdAndPaymentStatus(paymentId, PaymentStatus.SETTLED)).thenReturn(null);



        assertThatThrownBy(()->refundService.convertPaymentToRefund(merchantId,paymentId,amount)).isInstanceOf(ResourceNotFoundException.class);

    }
    @Test
    void validateRefundsTest_CompleteRefund(){
        Refunds refunds=new Refunds();
        Payment payment=new Payment();
        refunds.setPayment(payment);
        refunds.setRefundStatus(RefundStatus.PENDING);
        refunds.setRefundType(RefundType.COMPLETE);
        refunds.setAmount(BigDecimal.valueOf(2000));
        payment.setAmount(BigDecimal.valueOf(2000));
        payment.setAmountLeftToRefund(BigDecimal.valueOf(2000));

        when(restClientService.getCustomerBalance(refunds.getMerchantAccount())).thenReturn(BigDecimal.valueOf(10000));

        Refunds refunds1=refundService.validateRefunds(refunds);

        assertThat(refunds1.getRefundStatus()).isEqualTo(RefundStatus.VALIDATED);

    }

    @Test
    void validateRefundsTest_PartialRefund(){
        Refunds refunds=new Refunds();
        Payment payment=new Payment();
        refunds.setPayment(payment);
        refunds.setRefundStatus(RefundStatus.PENDING);
        refunds.setRefundType(RefundType.PARTIAL);
        refunds.setAmount(BigDecimal.valueOf(200));
        payment.setAmount(BigDecimal.valueOf(2000));
        payment.setAmountLeftToRefund(BigDecimal.valueOf(2000));

        when(restClientService.getCustomerBalance(refunds.getMerchantAccount())).thenReturn(BigDecimal.valueOf(10000));

        Refunds refunds1=refundService.validateRefunds(refunds);

        assertThat(refunds1.getRefundStatus()).isEqualTo(RefundStatus.VALIDATED);

    }
    @Test
    void validateRefundsTest_CompleteRefundFails(){
        Refunds refunds=new Refunds();
        Payment payment=new Payment();
        refunds.setPayment(payment);
        refunds.setRefundStatus(RefundStatus.PENDING);
        refunds.setRefundType(RefundType.COMPLETE);
        refunds.setAmount(BigDecimal.valueOf(200));
        payment.setAmount(BigDecimal.valueOf(2000));
        payment.setAmountLeftToRefund(BigDecimal.valueOf(2000));

        when(restClientService.getCustomerBalance(refunds.getMerchantAccount())).thenReturn(BigDecimal.valueOf(10000));

        Refunds refunds1=refundService.validateRefunds(refunds);

        assertThat(refunds1.getRefundStatus()).isEqualTo(RefundStatus.FAILED);

    }
    @Test
    void validateRefundsTest_PartialRefundFailed(){
        Refunds refunds=new Refunds();
        Payment payment=new Payment();
        refunds.setPayment(payment);
        refunds.setRefundStatus(RefundStatus.PENDING);
        refunds.setRefundType(RefundType.PARTIAL);
        refunds.setAmount(BigDecimal.valueOf(2000));
        payment.setAmount(BigDecimal.valueOf(2000));
        payment.setAmountLeftToRefund(BigDecimal.valueOf(2000));

        when(restClientService.getCustomerBalance(refunds.getMerchantAccount())).thenReturn(BigDecimal.valueOf(10000));

        Refunds refunds1=refundService.validateRefunds(refunds);

        assertThat(refunds1.getRefundStatus()).isEqualTo(RefundStatus.FAILED);

    }
    @Test
    void validateRefundsTest_InsufficientFunds(){
        Refunds refunds=new Refunds();
        Payment payment=new Payment();
        refunds.setPayment(payment);
        refunds.setRefundStatus(RefundStatus.PENDING);
        refunds.setRefundType(RefundType.PARTIAL);
        refunds.setAmount(BigDecimal.valueOf(200));
        payment.setAmount(BigDecimal.valueOf(2000));
        payment.setAmountLeftToRefund(BigDecimal.valueOf(2000));

        when(restClientService.getCustomerBalance(refunds.getMerchantAccount())).thenReturn(BigDecimal.valueOf(10));

        Refunds refunds1=refundService.validateRefunds(refunds);

        assertThat(refunds1.getRefundStatus()).isEqualTo(RefundStatus.FAILED);

    }

    @Test
    void validateRefundsTest_MoneyLeftToRefund(){
        Refunds refunds=new Refunds();
        Payment payment=new Payment();
        refunds.setPayment(payment);
        refunds.setRefundStatus(RefundStatus.PENDING);
        refunds.setRefundType(RefundType.PARTIAL);
        refunds.setAmount(BigDecimal.valueOf(200));
        payment.setAmount(BigDecimal.valueOf(2000));
        payment.setAmountLeftToRefund(BigDecimal.valueOf(0));

        when(restClientService.getCustomerBalance(refunds.getMerchantAccount())).thenReturn(BigDecimal.valueOf(2000));

        Refunds refunds1=refundService.validateRefunds(refunds);

        assertThat(refunds1.getRefundStatus()).isEqualTo(RefundStatus.FAILED);

    }

    @Test
    void payRefundsTest(){
        Refunds refunds=new Refunds();
        refunds.setRefundStatus(RefundStatus.VALIDATED);
        Payment payment=new Payment();
        refunds.setPayment(payment);
        refunds.setAmount(BigDecimal.valueOf(200));
        payment.setAmountLeftToRefund(BigDecimal.valueOf(2000));

        Refunds refunds1=refundService.payRefund(refunds);

        assertThat(payment.getAmountLeftToRefund()).isEqualTo(BigDecimal.valueOf(1800));

        verify(kafkaTemplate).send(eq("refund-completed"),any(RefundCompletedEvent.class));
    }



}
