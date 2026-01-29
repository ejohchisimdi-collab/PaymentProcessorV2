package com.chisimidi.payment.service.services;

import com.chisimidi.payment.service.models.Payment;
import com.chisimidi.payment.service.models.PaymentStatus;
import com.chisimidi.payment.service.repositories.PaymentRepository;
import com.chisimidi.payment.service.utils.FraudUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.convert.CurrencyConversion;
import javax.money.convert.MonetaryConversions;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Service
public class FraudService {
    private PaymentRepository paymentRepository;

    public FraudUtil calculateFraud(String customerToken, BigDecimal amount,String currency){
        int fraudScore=0;
        List<String>warnings=new ArrayList<>();

        List<Payment>payments=paymentRepository.findByCustomerAccount(customerToken);
        MonetaryAmount customer = Monetary.getDefaultAmountFactory()
                .setCurrency(currency)
                .setNumber(amount)
                .create();

        CurrencyConversion merchantConversion =
                MonetaryConversions.getConversion("USD");

        MonetaryAmount monetaryAmount = customer.with(merchantConversion);
       BigDecimal convertedAmount= monetaryAmount.getNumber()
                .numberValueExact(BigDecimal.class)
                .setScale(2, RoundingMode.HALF_EVEN);

        if(payments.isEmpty()&&convertedAmount.compareTo(BigDecimal.valueOf(10000))>0){
            fraudScore+=2;
            warnings.add("No transactions made with this account and a high transaction made today");
        }
        BigDecimal average=BigDecimal.ZERO;
        if(payments.size()>=10){
            for(int x=payments.size()-1;x>=payments.size()-10;x--){
                average=average.add(payments.get(x).getAmount());
            }
            average=average.divide(BigDecimal.valueOf(10),RoundingMode.HALF_EVEN);
            if(amount.compareTo(average.multiply(BigDecimal.valueOf(2)))>0){
                fraudScore+=2;
                warnings.add("Average spending is higher than current spending");
            }
            int paymentFrequencyForToday=0;
            int paymentFrequencyForLastWeek=0;
            for(int x=payments.size()-1;x>=payments.size()-10;x--){
                if (payments.get(x).getCreatedAt().toLocalDate().isEqual(LocalDateTime.now().toLocalDate())){
                    paymentFrequencyForToday+=1;
                }
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime start = now.minusDays(7);
                LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();

                LocalDateTime createdAt = payments.get(x).getCreatedAt();

                if (!createdAt.isBefore(start) && createdAt.isBefore(startOfToday)) {
                    paymentFrequencyForLastWeek++;
                }
            }
            if(paymentFrequencyForToday>paymentFrequencyForLastWeek*1.5){
                fraudScore+=2;
                warnings.add("Payment frequency is higher than usual");
            }
        }

        if (amount.remainder(BigDecimal.valueOf(100)).compareTo(BigDecimal.ZERO) == 0) {
            fraudScore += 1;
            warnings.add("Round Number");
        }

        if (payments.size()>=1) {
            if (payments.get(payments.size() - 1).getCreatedAt().isBefore(LocalDateTime.now().minusMonths(6))) {
                fraudScore += 2;
                warnings.add("Dormant reactivation");
            }
        }
            if(payments.size()>=3){
                if(ChronoUnit.MINUTES.between(payments.get(payments.size()-3).getCreatedAt(),payments.get(payments.size()-1).getCreatedAt())<1){
                    fraudScore+=1;
                    warnings.add("Three payments in less than one minute");
                }
            }
            if(payments.size()>=3) {
                if (payments.get(payments.size()-1).getPaymentStatus().equals(PaymentStatus.FAILED) &&
                        payments.get(payments.size() - 2).getPaymentStatus().equals(PaymentStatus.FAILED) &&
                        payments.get(payments.size() - 3).getPaymentStatus().equals(PaymentStatus.FAILED)){
                    fraudScore+=2;
                    warnings.add("Three failed consecutive payments");
                }

            }
            FraudUtil fraudUtil=new FraudUtil();
            fraudUtil.setFraudScore(fraudScore);
            fraudUtil.setWarnings(warnings);
            return fraudUtil;

        }




    }

