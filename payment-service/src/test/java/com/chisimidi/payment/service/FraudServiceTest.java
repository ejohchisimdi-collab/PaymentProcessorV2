package com.chisimidi.payment.service;

import com.chisimidi.payment.service.models.Payment;
import com.chisimidi.payment.service.models.PaymentStatus;
import com.chisimidi.payment.service.repositories.PaymentRepository;
import com.chisimidi.payment.service.services.FraudService;
import com.chisimidi.payment.service.utils.FraudUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.parameters.P;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FraudServiceTest {
    @Mock
    private PaymentRepository paymentRepository;
    @InjectMocks
    private FraudService fraudService;

    @Test
    void calculateFraudTest_BigFirstTransaction(){
        String customerToken="abc";
        BigDecimal amount=BigDecimal.valueOf(100002);
        String currency="USD";
        when(paymentRepository.findByCustomerAccount(customerToken)).thenReturn(new ArrayList<>());

        FraudUtil fraudUtil=fraudService.calculateFraud(customerToken,amount,currency);

        assertThat(fraudUtil.getFraudScore()).isEqualTo(2);
        assertThat(fraudUtil.getWarnings().get(0)).isEqualTo("No transactions made with this account and a high transaction made today");
    }

    @Test
    void calculateFraudTest_AverageSpendingIsHigherThanCurrentSpending(){
        Payment payment=new Payment();
        payment.setCreatedAt(LocalDateTime.now().minusDays(5));
        payment.setAmount(BigDecimal.valueOf(20));
        Payment payment1=new Payment();
        payment1.setCreatedAt(LocalDateTime.now().minusDays(5));
        payment1.setAmount(BigDecimal.valueOf(20));
        Payment payment2=new Payment();
        payment2.setCreatedAt(LocalDateTime.now().minusDays(5));
        payment2.setAmount(BigDecimal.valueOf(20));
        Payment payment3=new Payment();
        payment3.setCreatedAt(LocalDateTime.now().minusDays(5));
        payment3.setAmount(BigDecimal.valueOf(20));
        Payment payment4=new Payment();
        payment4.setCreatedAt(LocalDateTime.now().minusDays(5));
        payment4.setAmount(BigDecimal.valueOf(20));
        Payment payment5=new Payment();
        payment5.setCreatedAt(LocalDateTime.now().minusDays(5));
        payment5.setAmount(BigDecimal.valueOf(20));
        Payment payment6=new Payment();
        payment6.setCreatedAt(LocalDateTime.now().minusDays(5));
        payment6.setAmount(BigDecimal.valueOf(20));
        Payment payment7=new Payment();
        payment7.setCreatedAt(LocalDateTime.now().minusDays(5));
        payment7.setAmount(BigDecimal.valueOf(20));
        Payment payment8=new Payment();
        payment.setCreatedAt(LocalDateTime.now());
        payment8.setPaymentStatus(PaymentStatus.SETTLED);
        payment8.setAmount(BigDecimal.valueOf(20));
        payment8.setCreatedAt(LocalDateTime.now());
        Payment payment9=new Payment();
        payment9.setPaymentStatus(PaymentStatus.SETTLED);
        payment9.setAmount(BigDecimal.valueOf(20));
        payment9.setCreatedAt(LocalDateTime.now());
        Payment payment10=new Payment();
        payment10.setPaymentStatus(PaymentStatus.SETTLED);
        payment10.setAmount(BigDecimal.valueOf(20));
        payment10.setCreatedAt(LocalDateTime.now().plusMinutes(3));

        BigDecimal amount=BigDecimal.valueOf(20001);
        String currency="USD";
        String customerToken="abc";

        when(paymentRepository.findByCustomerAccount(customerToken)).thenReturn(List.of(payment1,payment2,payment3,payment4,payment5,payment6,payment7,payment8,payment9,payment10));

        FraudUtil fraudUtil=fraudService.calculateFraud(customerToken,amount,currency);

        assertThat(fraudUtil.getWarnings().get(0)).isEqualTo("Average spending is higher than current spending");
        assertThat(fraudUtil.getFraudScore()).isEqualTo(2);

    }

    @Test
    void calculateFraudTest_PaymentFrequencyIsHigherThanUsual(){
        Payment payment=new Payment();
        payment.setCreatedAt(LocalDateTime.now().minusDays(5));
        payment.setAmount(BigDecimal.valueOf(20));
        Payment payment1=new Payment();
        payment1.setCreatedAt(LocalDateTime.now().minusDays(5));
        payment1.setAmount(BigDecimal.valueOf(20));
        Payment payment2=new Payment();
        payment2.setCreatedAt(LocalDateTime.now());
        payment2.setAmount(BigDecimal.valueOf(20));
        Payment payment3=new Payment();
        payment3.setCreatedAt(LocalDateTime.now());
        payment3.setAmount(BigDecimal.valueOf(20));
        Payment payment4=new Payment();
        payment4.setCreatedAt(LocalDateTime.now());
        payment4.setAmount(BigDecimal.valueOf(20));
        Payment payment5=new Payment();
        payment5.setCreatedAt(LocalDateTime.now());
        payment5.setAmount(BigDecimal.valueOf(20));
        Payment payment6=new Payment();
        payment6.setCreatedAt(LocalDateTime.now());
        payment6.setAmount(BigDecimal.valueOf(20));
        Payment payment7=new Payment();
        payment7.setCreatedAt(LocalDateTime.now());
        payment7.setAmount(BigDecimal.valueOf(20));
        Payment payment8=new Payment();
        payment.setCreatedAt(LocalDateTime.now());
        payment8.setPaymentStatus(PaymentStatus.SETTLED);
        payment8.setAmount(BigDecimal.valueOf(20));
        payment8.setCreatedAt(LocalDateTime.now());
        Payment payment9=new Payment();
        payment9.setPaymentStatus(PaymentStatus.SETTLED);
        payment9.setAmount(BigDecimal.valueOf(20));
        payment9.setCreatedAt(LocalDateTime.now());
        Payment payment10=new Payment();
        payment10.setPaymentStatus(PaymentStatus.SETTLED);
        payment10.setAmount(BigDecimal.valueOf(20));
        payment10.setCreatedAt(LocalDateTime.now().plusMinutes(3));

        BigDecimal amount=BigDecimal.valueOf(20);
        String currency="USD";
        String customerToken="abc";

        when(paymentRepository.findByCustomerAccount(customerToken)).thenReturn(List.of(payment1,payment2,payment3,payment4,payment5,payment6,payment7,payment8,payment9,payment10));

        FraudUtil fraudUtil=fraudService.calculateFraud(customerToken,amount,currency);

        assertThat(fraudUtil.getWarnings().get(0)).isEqualTo("Payment frequency is higher than usual");
        assertThat(fraudUtil.getFraudScore()).isEqualTo(2);

    }
    @Test
    void calculateFraudTest_ThreePaymentsInLessThanOneMinute(){

        Payment payment7=new Payment();
        payment7.setCreatedAt(LocalDateTime.now());
        payment7.setAmount(BigDecimal.valueOf(20));
        Payment payment8=new Payment();
        payment8.setPaymentStatus(PaymentStatus.SETTLED);
        payment8.setAmount(BigDecimal.valueOf(20));
        payment8.setCreatedAt(LocalDateTime.now());
        Payment payment9=new Payment();
        payment9.setPaymentStatus(PaymentStatus.SETTLED);
        payment9.setAmount(BigDecimal.valueOf(20));
        payment9.setCreatedAt(LocalDateTime.now());
        Payment payment10=new Payment();
        payment10.setPaymentStatus(PaymentStatus.SETTLED);
        payment10.setAmount(BigDecimal.valueOf(20));
        payment10.setCreatedAt(LocalDateTime.now());

        BigDecimal amount=BigDecimal.valueOf(20);
        String currency="USD";
        String customerToken="abc";

        when(paymentRepository.findByCustomerAccount(customerToken)).thenReturn(List.of(payment7,payment8,payment9,payment10));

        FraudUtil fraudUtil=fraudService.calculateFraud(customerToken,amount,currency);

        assertThat(fraudUtil.getWarnings().get(0)).isEqualTo("Three payments in less than one minute");
        assertThat(fraudUtil.getFraudScore()).isEqualTo(1);

    }
    @Test
    void calculateFraudTest_ThreeConsecutiveFailedPayments(){

        Payment payment7=new Payment();
        payment7.setCreatedAt(LocalDateTime.now());
        payment7.setAmount(BigDecimal.valueOf(20));
        Payment payment8=new Payment();
        payment8.setPaymentStatus(PaymentStatus.FAILED);
        payment8.setAmount(BigDecimal.valueOf(20));
        payment8.setCreatedAt(LocalDateTime.now());
        Payment payment9=new Payment();
        payment9.setPaymentStatus(PaymentStatus.FAILED);
        payment9.setAmount(BigDecimal.valueOf(20));
        payment9.setCreatedAt(LocalDateTime.now());
        Payment payment10=new Payment();
        payment10.setPaymentStatus(PaymentStatus.FAILED);
        payment10.setAmount(BigDecimal.valueOf(20));
        payment10.setCreatedAt(LocalDateTime.now().plusMinutes(3));

        BigDecimal amount=BigDecimal.valueOf(20);
        String currency="USD";
        String customerToken="abc";

        when(paymentRepository.findByCustomerAccount(customerToken)).thenReturn(List.of(payment7,payment8,payment9,payment10));

        FraudUtil fraudUtil=fraudService.calculateFraud(customerToken,amount,currency);

        assertThat(fraudUtil.getWarnings().get(0)).isEqualTo("Three failed consecutive payments");
        assertThat(fraudUtil.getFraudScore()).isEqualTo(2);

    }
    @Test
    void calculateFraudTest_RoundNumber(){
        String customerToken="abc";
        BigDecimal amount=BigDecimal.valueOf(10000);
        String currency="USD";
        when(paymentRepository.findByCustomerAccount(customerToken)).thenReturn(List.of(new Payment()));

        FraudUtil fraudUtil=fraudService.calculateFraud(customerToken,amount,currency);

        assertThat(fraudUtil.getFraudScore()).isEqualTo(1);
        assertThat(fraudUtil.getWarnings().get(0)).isEqualTo("Round Number");
    }
    @Test
    void calculateFraudTest_DormantReactivation(){
        String customerToken="abc";
        BigDecimal amount=BigDecimal.valueOf(10002);
        String currency="USD";
        Payment payment=new Payment();
        payment.setCreatedAt(LocalDateTime.now().minusMonths(7));
        when(paymentRepository.findByCustomerAccount(customerToken)).thenReturn(List.of(payment));

        FraudUtil fraudUtil=fraudService.calculateFraud(customerToken,amount,currency);

        assertThat(fraudUtil.getFraudScore()).isEqualTo(2);
        assertThat(fraudUtil.getWarnings().get(0)).isEqualTo("Dormant reactivation");
    }
}
