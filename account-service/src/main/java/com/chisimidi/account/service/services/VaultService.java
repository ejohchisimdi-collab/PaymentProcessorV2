package com.chisimidi.account.service.services;

import com.chisimdi.payment.events.FailAuthorizationEvent;
import com.chisimdi.payment.events.LedgerPaymentEvent;
import com.chisimdi.payment.events.RefundCompletedEvent;
import com.chisimdi.payment.events.SettlementEvent;
import com.chisimidi.account.service.excptions.ResourceNotFoundException;
import com.chisimidi.account.service.mappers.VaultMapper;
import com.chisimidi.account.service.models.*;
import com.chisimidi.account.service.repositories.AccountRepository;
import com.chisimidi.account.service.repositories.EventsIdempotencyRepository;
import com.chisimidi.account.service.repositories.VaultRepository;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class VaultService {
    private VaultRepository vaultRepository;
    private VaultMapper vaultMapper;
    private AccountEncoderService accountEncoderService;
    private AccountRepository accountRepository;
    private KafkaTemplate<String,Object>kafkaTemplate;
    private EventsIdempotencyRepository idempotencyRepository;


    public List<VaultDTO> findAllVaults(int pageNumber, int size){
        Page<Vault>vaults=vaultRepository.findAll(PageRequest.of(pageNumber, size));
        return vaultMapper.toVaultDTOList(vaults.getContent());
    }

    public BigDecimal findAccountBalance(String token){
        Account account=accountRepository.findById(accountEncoderService.decode(token)).orElse(null);
        return account.getMoneyRemaining();
    }

    public Boolean doesMerchantAccountExist(String token,int merchantId){
        return accountRepository.existsByAccountNumberAndUserIdAndOwnerType(accountEncoderService.decode(token),merchantId,OwnerType.MERCHANT);
    }

    public Boolean doesCustomerAccountExist(String token){
        return vaultRepository.existsByTokenAndOwnerType(token,OwnerType.CUSTOMER);
    }
    public String getAccountCurrency(String token){
        Account account=accountRepository.findById(accountEncoderService.decode(token)).orElse(null);
        return account.getCurrency();
    }
    public AccountType getAccountType(String token){
        return vaultRepository.findByToken(token).getAccountType();
    }

    @Transactional
    @Retryable(retryFor = OptimisticLockException.class,maxAttempts = 5,backoff = @Backoff(delay = 2,multiplier = 4))
    public void reserveFunds(String token, BigDecimal amount){
        Account account=accountRepository.findById(accountEncoderService.decode(token)).orElse(null);
        account.setMoneyRemaining(account.getMoneyRemaining().subtract(amount));
        accountRepository.save(account);
    }

    @Retryable(retryFor = OptimisticLockException.class,maxAttempts = 5,backoff = @Backoff(delay = 2,multiplier = 4))
    @KafkaListener(topics = "payment-settled")
public void settleMerchant(SettlementEvent settlementEvent){
        EventsIdempotency eventsIdempotency=idempotencyRepository.findById(settlementEvent.getId()).orElse(null);
        if(eventsIdempotency!=null){
            return;
        }

        Account account=accountRepository.findById(accountEncoderService.decode(settlementEvent.getMerchantToken())).orElse(null);
        if(account==null){
            throw new ResourceNotFoundException("Account not found");
        }
        account.setPendingAccount(account.getPendingAccount().add(settlementEvent.getAmount()));
        accountRepository.save(account);
        EventsIdempotency idempotency=new EventsIdempotency();
        idempotency.setId(settlementEvent.getId());
        idempotency.setContext("Settle merchant Event");
        idempotencyRepository.save(idempotency);
    }

    @KafkaListener(topics = "authorization-failed")
    public void releaseFunds(FailAuthorizationEvent failAuthorizationEvent){
        EventsIdempotency eventsIdempotency=idempotencyRepository.findById(failAuthorizationEvent.getId()).orElse(null);
        if(eventsIdempotency!=null){
            return;
        }
        Account account=accountRepository.findById(accountEncoderService.decode(failAuthorizationEvent.getCustomerAccount())).orElse(null);
        if(account==null){
            throw new ResourceNotFoundException("Account not found");
        }
        account.setMoneyRemaining(account.getMoneyRemaining().add(failAuthorizationEvent.getAmount()));
        accountRepository.save(account);
        EventsIdempotency idempotency=new EventsIdempotency();
        idempotency.setId(failAuthorizationEvent.getId());
        idempotency.setContext("Fail authorization Event");
        idempotencyRepository.save(idempotency);

    }

    @KafkaListener(topics = "refund-completed")
    public void processRefund(RefundCompletedEvent completedEvent){
        EventsIdempotency eventsIdempotency=idempotencyRepository.findById(completedEvent.getId()).orElse(null);
        if(eventsIdempotency!=null){
            return;
        }

        Account account=accountRepository.findById(accountEncoderService.decode(completedEvent.getCustomerAccountToken())).orElse(null);
        if(account==null){
            throw new ResourceNotFoundException("Account not found");
        }
        account.setMoneyRemaining(account.getMoneyRemaining().add(completedEvent.getAmount()));
        accountRepository.save(account);
        EventsIdempotency idempotency=new EventsIdempotency();
        idempotency.setId(completedEvent.getId());
        idempotency.setContext("Refund Completed Event");
        idempotencyRepository.save(idempotency);

    }

    @KafkaListener(topics = "payout-initiated")
    public void initiatePayout(LedgerPaymentEvent paymentEvent){
        Account account=accountRepository.findById(accountEncoderService.decode(paymentEvent.getAccountTo())).orElse(null);
        if(account==null){
            throw new ResourceNotFoundException("Account not found");
        }
        account.setPendingAccount(account.getPendingAccount().subtract(paymentEvent.getAmount()));
        account.setMoneyRemaining(account.getMoneyRemaining().add(paymentEvent.getAmount()));
        accountRepository.save(account);
        kafkaTemplate.send("payout-completed",paymentEvent);
        EventsIdempotency idempotency=new EventsIdempotency();
        idempotency.setId(paymentEvent.getId());
        idempotency.setContext("Payout Initiated Event");
        idempotencyRepository.save(idempotency);


    }


}
