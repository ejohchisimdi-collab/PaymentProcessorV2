package com.chisimidi.payment.service.services;

import com.chisimdi.payment.events.AuthorizationEvent;
import com.chisimdi.payment.events.CaptureEvent;
import com.chisimdi.payment.events.FailAuthorizationEvent;
import com.chisimdi.payment.events.SettlementEvent;
import com.chisimidi.payment.service.exceptions.ConflictException;
import com.chisimidi.payment.service.exceptions.ResourceNotFoundException;
import com.chisimidi.payment.service.mappers.PaymentMapper;
import com.chisimidi.payment.service.models.*;
import com.chisimidi.payment.service.repositories.PaymentIdempotencyRepository;
import com.chisimidi.payment.service.repositories.PaymentRepository;
import com.chisimidi.payment.service.utils.AccountType;
import com.chisimidi.payment.service.utils.CaptureType;
import com.chisimidi.payment.service.utils.FraudUtil;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;

import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.convert.CurrencyConversion;
import javax.money.convert.MonetaryConversions;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PaymentService {
    private PaymentMapper paymentMapper;
    private PaymentRepository paymentRepository;
    private RestClientService restClientService;
    private FraudService fraudService;
    private KafkaTemplate<String,Object>kafkaTemplate;
    private PaymentIdempotencyRepository paymentIdempotencyRepository;

    public Payment pendingProcessor(int merchantId, String merchantAccountToken, String customerAccountToken, BigDecimal amount){
        Payment payment=new Payment();
        if(!restClientService.doesMerchantAndAccountExist(merchantAccountToken,merchantId)){
            throw new ResourceNotFoundException("Merchant account with  merchant id "+merchantId+" and account token"+ merchantAccountToken + "not found");
        }
        if(!restClientService.doesCustomerAccountExist(customerAccountToken)){
            throw new ResourceNotFoundException("Customer account with token "+customerAccountToken+" not found");
        }
        BigDecimal transactionLimit=restClientService.getTransactionLimit(merchantId);
        String merchantCurrency=restClientService.getMerchantCurrency(merchantId);
        String customerCurrency=restClientService.getCustomerAccountCurrency(customerAccountToken);

        if(!merchantCurrency.equals(customerCurrency)){
            BigDecimal currencyConversionFee=(amount.multiply(BigDecimal.valueOf(4))).divide(BigDecimal.valueOf(100),RoundingMode.HALF_EVEN);
            payment.setConversionFee(currencyConversionFee);
            MonetaryAmount customer = Monetary.getDefaultAmountFactory()
                    .setCurrency(customerCurrency)
                    .setNumber(amount)
                    .create();

            CurrencyConversion merchantConversion =
                    MonetaryConversions.getConversion(merchantCurrency);

            MonetaryAmount monetaryAmount = customer.with(merchantConversion);
            BigDecimal amountAfterConversion= monetaryAmount.getNumber()
                    .numberValueExact(BigDecimal.class)
                    .setScale(2, RoundingMode.HALF_EVEN);
            payment.setAmount(amount);
            payment.setAmountAfterConversion(amountAfterConversion);
        }
        else {
            payment.setAmount(amount);
            payment.setAmountAfterConversion(amount);
            payment.setConversionFee(BigDecimal.ZERO);
        }
        if(transactionLimit.compareTo(payment.getAmount())<0){
            throw new ConflictException("Payment with amount in "+merchantCurrency+ ":"+payment.getAmount()+" has exceeded transaction limit ");
        }

        AccountType accountType=restClientService.getCustomerAccountType(customerAccountToken);
        if(accountType.equals(AccountType.CREDIT)){
            payment.setPaymentStatus(PaymentStatus.CREDIT_PENDING);
            payment.setAccountType(AccountType.CREDIT);
        }
        else {
            payment.setPaymentStatus(PaymentStatus.BANK_PENDING);
            payment.setAccountType(AccountType.BANK);
        }
        payment.setAmountLeftToRefund(payment.getAmount());
        payment.setCurrency(customerCurrency);
        payment.setPaymentType(PaymentType.IMMEDIATE);
        payment.setPlatformFee((payment.getAmount().multiply(BigDecimal.valueOf(3)).divide(BigDecimal.valueOf(100),RoundingMode.HALF_EVEN)));
        payment.setCustomerAccount(customerAccountToken);
        payment.setMerchantAccount(merchantAccountToken);
        payment.setMerchantId(merchantId);
        paymentRepository.save(payment);
        return payment;

    }

    public Payment validateProcessor(Payment payment){
        if(payment.getPaymentStatus().equals(PaymentStatus.CREDIT_PENDING)){
            FraudUtil fraudUtil=fraudService.calculateFraud(payment.getCustomerAccount(),payment.getAmount(),payment.getCurrency());
           BigDecimal customerBalance= restClientService.getCustomerBalance(payment.getCustomerAccount());
            payment.setWarnings(fraudUtil.getWarnings());
           if(fraudUtil.getFraudScore()>=5 ){
                payment.setPaymentStatus(PaymentStatus.FAILED);

            }
            if(customerBalance.compareTo(payment.getAmount().add(payment.getConversionFee()))<0){
                payment.setPaymentStatus(PaymentStatus.FAILED);
                payment.getWarnings().add("Insufficient funds");
            }
            if(!payment.getPaymentStatus().equals(PaymentStatus.FAILED)){
                payment.setPaymentStatus(PaymentStatus.VALIDATED);
            }
        }
        paymentRepository.save(payment);
        return payment;
    }

    public Payment authorisedProcessor(Payment payment){
        if(payment.getPaymentStatus().equals(PaymentStatus.VALIDATED)) {
            payment.setAuthorizationDueDate(LocalDateTime.now().plusDays(7));
            payment.setPaymentStatus(PaymentStatus.AUTHORISED);
            restClientService.reserveFunds(payment.getCustomerAccount(), payment.getAmount().add(payment.getConversionFee()));
        }
        paymentRepository.save(payment);
        AuthorizationEvent authorizationEvent=new AuthorizationEvent();
        authorizationEvent.setId(UUID.randomUUID().toString());
        authorizationEvent.setPaymentId(payment.getId());
        authorizationEvent.setAmount(payment.getAmountAfterConversion());
        authorizationEvent.setMerchantId(payment.getMerchantId());
        authorizationEvent.setAccountFrom(payment.getCustomerAccount());
        authorizationEvent.setAccountTo(payment.getMerchantAccount());
        kafkaTemplate.send("authorization-completed",authorizationEvent);
        return payment;
    }

    @Scheduled(cron = "*/5 * * * * *")
    public void failPayments(){
        List<Payment>payments=paymentRepository.findByAuthorizationDueDateBeforeAndPaymentStatus(LocalDateTime.now(),PaymentStatus.AUTHORISED);
        List<Payment>payments1=new ArrayList<>();
        for(Payment payment:payments){
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payments1.add(payment);
            FailAuthorizationEvent failAuthorizationEvent=new FailAuthorizationEvent();
            failAuthorizationEvent.setId(UUID.randomUUID().toString());
            failAuthorizationEvent.setAmount(payment.getAmount());
            failAuthorizationEvent.setPaymentId(payment.getId());
            failAuthorizationEvent.setMerchantId(payment.getMerchantId());
            failAuthorizationEvent.setCustomerAccount(payment.getCustomerAccount());
            kafkaTemplate.send("authorization-failed",failAuthorizationEvent);

        }
        paymentRepository.saveAll(payments1);

    }

    public Payment capturePayments(Payment payment){
      CaptureType captureType= restClientService.getCaptureType(payment.getMerchantId());
      if(captureType.equals(CaptureType.AUTOMATIC)&&payment.getPaymentStatus().equals(PaymentStatus.AUTHORISED)){
          payment.setPaymentStatus(PaymentStatus.CAPTURED);
      }
      paymentRepository.save(payment);
        CaptureEvent captureEvent=new CaptureEvent();
        captureEvent.setId(UUID.randomUUID().toString());
        captureEvent.setPaymentId(payment.getId());
        captureEvent.setMerchantId(payment.getMerchantId());
        captureEvent.setPlatformFee(payment.getPlatformFee());
        captureEvent.setMerchantAccount(payment.getMerchantAccount());
        captureEvent.setCustomerAccount(payment.getCustomerAccount());
        captureEvent.setTotalAmount(payment.getAmountAfterConversion());
      kafkaTemplate.send("payment-captured",captureEvent);
      return payment;
    }

    public Payment settlePayment(Payment payment){
        if(payment.getPaymentStatus().equals(PaymentStatus.CAPTURED)){
            payment.setPaymentStatus(PaymentStatus.SETTLED);
            paymentRepository.save(payment);
            SettlementEvent settlementEvent=new SettlementEvent();
            settlementEvent.setId(UUID.randomUUID().toString());
            settlementEvent.setPaymentId(payment.getId());
            settlementEvent.setAmount(payment.getAmountAfterConversion().subtract(payment.getPlatformFee()));
            settlementEvent.setMerchantToken(payment.getMerchantAccount());
            settlementEvent.setMerchantId(payment.getMerchantId());
            settlementEvent.setCustomerToken(payment.getCustomerAccount());
            settlementEvent.setTotalAmount(payment.getAmountAfterConversion());
            kafkaTemplate.send("payment-settled",settlementEvent);
        }
        paymentRepository.save(payment);
        return payment;
    }

    public Payment manualCapturing(int paymentId,int merchantId){
        Payment payment=paymentRepository.findByIdAndPaymentStatusAndMerchantId(paymentId,PaymentStatus.AUTHORISED,merchantId);
        if(payment==null){
            throw new ResourceNotFoundException("Payment with id "+paymentId+" and payment status authorised not found");
        }
        if(payment.getAuthorizationDueDate().isBefore(LocalDateTime.now())){
            payment.setPaymentStatus(PaymentStatus.FAILED);
            throw new ConflictException("Payment authorization has been expired");
        }
        else {
            payment.setPaymentStatus(PaymentStatus.CAPTURED);
        }
        paymentRepository.save(payment);
        CaptureEvent captureEvent=new CaptureEvent();
        captureEvent.setMerchantId(payment.getMerchantId());
        captureEvent.setId(UUID.randomUUID().toString());
        captureEvent.setPaymentId(payment.getId());
        captureEvent.setPlatformFee(payment.getPlatformFee());
        captureEvent.setMerchantAccount(payment.getMerchantAccount());
        captureEvent.setCustomerAccount(payment.getCustomerAccount());
        captureEvent.setTotalAmount(payment.getAmountAfterConversion());
        kafkaTemplate.send("payment-captured",captureEvent);
        return payment;

    }

    @Retryable(retryFor = OptimisticLockException.class,maxAttempts = 3,backoff = @Backoff(delay = 2,multiplier = 3))
    @Transactional
    public PaymentDTO processPayment(String apiKey, String idempotencyKey, int merchantId, String merchantAccountToken, String customerAccountToken, BigDecimal amount){
     String hamcSecret=restClientService.getMerchantSecret(merchantId);

     if(!hamcSecret.equals(apiKey)){
         throw new AuthorizationDeniedException("Api key not valid");
     }

        PaymentIdempotency paymentIdempotency=paymentIdempotencyRepository.findByIdempotencyKey(idempotencyKey);
        if(paymentIdempotency!=null){
            return paymentMapper.toPaymentDTO(paymentIdempotency.getPayment());
        }


        Payment payment=pendingProcessor(merchantId, merchantAccountToken, customerAccountToken, amount);
        payment=validateProcessor(payment);
        payment=authorisedProcessor(payment);
        payment=capturePayments(payment);
        payment=settlePayment(payment);
        PaymentIdempotency idempotency=new PaymentIdempotency();
        idempotency.setPayment(payment);
        idempotency.setIdempotencyKey(idempotencyKey);
        paymentIdempotencyRepository.save(idempotency);
        return paymentMapper.toPaymentDTO(payment);
    }

    @Retryable(retryFor = OptimisticLockException.class,maxAttempts = 3,backoff = @Backoff(delay = 2,multiplier = 3))
    @Transactional
    public PaymentDTO manual(int paymentId,int merchantId){
        Payment payment=manualCapturing(paymentId,merchantId);
        settlePayment(payment);
        return paymentMapper.toPaymentDTO(payment);
    }


    @Scheduled(cron = "*/5 * * * * *")
    public void settleBankPayments(){
        List<Payment>payments=paymentRepository.findByPaymentStatusAndDone(PaymentStatus.BANK_PENDING,false);
        List<Payment>payments1=new ArrayList<>();
        for(Payment payment : payments){
            FraudUtil fraudUtil=fraudService.calculateFraud(payment.getCustomerAccount(), payment.getAmount(), payment.getCurrency());
            BigDecimal customerBalance= restClientService.getCustomerBalance(payment.getCustomerAccount());
            payment.setWarnings(fraudUtil.getWarnings());
            if(fraudUtil.getFraudScore()>=5 ){
                payment.setPaymentStatus(PaymentStatus.FAILED);

            }
            if(customerBalance.compareTo(payment.getAmount().add(payment.getConversionFee()))<0){
                payment.setPaymentStatus(PaymentStatus.FAILED);
                payment.getWarnings().add("Insufficient funds");
            }
            if(!payment.getPaymentStatus().equals(PaymentStatus.FAILED)){
                restClientService.reserveFunds(payment.getCustomerAccount(),payment.getAmount().subtract(payment.getConversionFee()));
                payment.setPaymentStatus(PaymentStatus.SETTLED);
                SettlementEvent settlementEvent=new SettlementEvent();
                settlementEvent.setAmount(payment.getAmount());
                settlementEvent.setId(UUID.randomUUID().toString());
                settlementEvent.setPaymentId(payment.getId());
                settlementEvent.setAmount(payment.getAmountAfterConversion().subtract(payment.getPlatformFee()));
                settlementEvent.setMerchantToken(payment.getMerchantAccount());
                settlementEvent.setMerchantId(payment.getMerchantId());
                kafkaTemplate.send("payment-settled",settlementEvent);
                CaptureEvent captureEvent=new CaptureEvent();
                captureEvent.setId(UUID.randomUUID().toString());
                captureEvent.setPaymentId(payment.getId());
                captureEvent.setPlatformFee(payment.getPlatformFee());
                captureEvent.setMerchantAccount(payment.getMerchantAccount());
                captureEvent.setCustomerAccount(payment.getCustomerAccount());
                captureEvent.setTotalAmount(payment.getAmount());
                kafkaTemplate.send("payment-captured",captureEvent);
            }
            payment.setDone(true);
            payments1.add(payment);

        }
        paymentRepository.saveAll(payments1);

        }

        public List<PaymentDTO>findPaymentsByMerchant(int merchantId,int pageNumber,int size){
            Page<Payment>payments=paymentRepository.findByMerchantId(merchantId, PageRequest.of(pageNumber,size));
            return paymentMapper.toPaymentDTOList(payments.getContent());
        }
    }


