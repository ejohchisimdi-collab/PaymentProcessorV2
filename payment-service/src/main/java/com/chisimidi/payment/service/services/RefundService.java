package com.chisimidi.payment.service.services;

import com.chisimdi.payment.events.RefundCompletedEvent;
import com.chisimidi.payment.service.exceptions.ResourceNotFoundException;
import com.chisimidi.payment.service.mappers.RefundMapper;
import com.chisimidi.payment.service.models.*;
import com.chisimidi.payment.service.repositories.PaymentIdempotencyRepository;
import com.chisimidi.payment.service.repositories.PaymentRepository;
import com.chisimidi.payment.service.repositories.RefundIdempotencyRepository;
import com.chisimidi.payment.service.repositories.RefundsRepository;
import com.chisimidi.payment.service.utils.RefundType;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;

import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.convert.CurrencyConversion;
import javax.money.convert.MonetaryConversions;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
@Service
@AllArgsConstructor
public class RefundService {
    private RestClientService restClientService;
    private RefundMapper refundMapper;
    private RefundsRepository refundsRepository;
    private PaymentRepository paymentRepository;
    private KafkaTemplate<String,Object>kafkaTemplate;
    private RefundIdempotencyRepository refundIdempotencyRepository;

    public Refunds convertPaymentToRefund(int merchantId,int paymentId, BigDecimal amount){
        Payment payment=paymentRepository.findByIdAndPaymentStatus(paymentId, PaymentStatus.SETTLED);
        if(payment==null){
            throw new ResourceNotFoundException("Payment with Id "+paymentId+" amd status settled not found");
        }
        if(!restClientService.doesMerchantSettingExist(merchantId)){
            throw new ResourceNotFoundException("Merchant setting for merchant with id "+merchantId+" not found");
        }
        RefundType refundType=restClientService.getMerchantRefundType(merchantId);
        String merchantCurrency=restClientService.getMerchantCurrency(merchantId);
        Refunds refunds=new Refunds();
        if(!merchantCurrency.equals(payment.getCurrency())){

            MonetaryAmount merchant = Monetary.getDefaultAmountFactory()
                    .setCurrency(merchantCurrency)
                    .setNumber(amount)
                    .create();

            CurrencyConversion customerConversion =
                    MonetaryConversions.getConversion(payment.getCurrency());

            MonetaryAmount monetaryAmount = merchant.with(customerConversion);
            BigDecimal amountAfterConversion= monetaryAmount.getNumber()
                    .numberValueExact(BigDecimal.class)
                    .setScale(2, RoundingMode.HALF_EVEN);

            refunds.setAmountAfterConversion(amountAfterConversion);
        }
        else {
            refunds.setAmountAfterConversion(amount);
        }

        refunds.setRefundStatus(RefundStatus.PENDING);
        refunds.setCurrency(payment.getCurrency());
        refunds.setAmount(amount);
        refunds.setPayment(payment);
        refunds.setCustomerAccount(payment.getCustomerAccount());
        refunds.setMerchantAccount(payment.getMerchantAccount());
        refunds.setMerchantId(merchantId);
        refunds.setRefundType(refundType);
        refunds.setConversionFee(payment.getConversionFee());
        refunds.setPlatformFee(payment.getPlatformFee());
        refundsRepository.save(refunds);
        return refunds;
    }

    public Refunds validateRefunds(Refunds refunds){
        if(refunds.getRefundStatus().equals(RefundStatus.PENDING)){
            Payment payment=refunds.getPayment();

            BigDecimal merchantBalance=restClientService.getCustomerBalance(refunds.getMerchantAccount());
            if(merchantBalance.compareTo(refunds.getAmount())<0){
                refunds.setRefundStatus(RefundStatus.FAILED);
                refunds.getWarnings().add("Insufficient Funds");
            }

            if(payment.getAmountLeftToRefund().compareTo(BigDecimal.ZERO)<=0){
                refunds.setRefundStatus(RefundStatus.FAILED);
                refunds.getWarnings().add("No money Left to refund");
            }

            if(payment.getAmountLeftToRefund().compareTo(refunds.getAmount())<0){
                refunds.setRefundStatus(RefundStatus.FAILED);
                refunds.getWarnings().add("Amount more than money Left to refund");
            }


            if(refunds.getRefundType().equals(RefundType.COMPLETE)){
                if(refunds.getAmount().compareTo(payment.getAmount())!=0){
                    refunds.setRefundStatus(RefundStatus.FAILED);
                    refunds.getWarnings().add("Refund type is complete however amount sent is less than total amount");
                }

            }
            if(refunds.getRefundType().equals(RefundType.PARTIAL)){
                if(refunds.getAmount().compareTo(payment.getAmount())==0){
                    refunds.setRefundStatus(RefundStatus.FAILED);
                    refunds.getWarnings().add("RefundType is partial however amount is a complete payment");
                }
            }
            if(!refunds.getRefundStatus().equals(RefundStatus.FAILED)){
                refunds.setRefundStatus(RefundStatus.VALIDATED);
            }
        }
        refundsRepository.save(refunds);
        return refunds;
    }

    public Refunds payRefund( Refunds refunds){
        if(refunds.getRefundStatus().equals(RefundStatus.VALIDATED)){
            RefundCompletedEvent completedEvent=new RefundCompletedEvent();
            completedEvent.setId(UUID.randomUUID().toString());
            completedEvent.setAmount(refunds.getAmountAfterConversion());
            completedEvent.setCustomerAccountToken(refunds.getCustomerAccount());
            completedEvent.setMerchantId(refunds.getMerchantId());
            completedEvent.setPaymentId(refunds.getPayment().getId());
            completedEvent.setMerchantToken(refunds.getMerchantAccount());
            completedEvent.setTotalAmount(refunds.getAmount());
            refunds.setRefundStatus(RefundStatus.COMPLETED);
            Payment payment=refunds.getPayment();
            payment.setAmountLeftToRefund(payment.getAmountLeftToRefund().subtract(refunds.getAmount()));
            paymentRepository.save(payment);
            kafkaTemplate.send("refund-completed",completedEvent);
            restClientService.reserveFunds(refunds.getMerchantAccount(),refunds.getAmount());
        }
        refundsRepository.save(refunds);
        return refunds;
    }

    @Retryable(retryFor = OptimisticLockException.class,maxAttempts = 3,backoff = @Backoff(delay = 2,multiplier = 3))
    @Transactional
    public RefundDTO processRefunds(String apiKey,String idempotencyKey,int merchantId,int paymentId, BigDecimal amount){
        String hamcSecret=restClientService.getMerchantSecret(merchantId);

        if(!hamcSecret.equals(apiKey)){
            throw new AuthorizationDeniedException("Api key not valid");
        }

        RefundIdempotency refundIdempotency=refundIdempotencyRepository.findByIdempotencyKey(idempotencyKey);
       if(refundIdempotency!=null){
           return refundMapper.toRefundDTO(refundIdempotency.getRefunds());
       }

        Refunds refunds= convertPaymentToRefund(merchantId, paymentId, amount);
        refunds=validateRefunds(refunds);
        refunds=payRefund(refunds);
        RefundIdempotency idempotency=new RefundIdempotency();
        idempotency.setIdempotencyKey(idempotencyKey);
        idempotency.setRefunds(refunds);
        refundIdempotencyRepository.save(idempotency);
        return refundMapper.toRefundDTO(refunds);
    }

    public List<RefundDTO>findRefundsByMerchant(int merchantId,int pageNumber,int size){
        Page<Refunds>refunds=refundsRepository.findByMerchantId(merchantId, PageRequest.of(pageNumber,size));
        return refundMapper.toRefundDTOList(refunds.getContent());
    }
}
