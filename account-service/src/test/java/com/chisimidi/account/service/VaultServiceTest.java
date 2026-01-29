package com.chisimidi.account.service;

import com.chisimdi.payment.events.FailAuthorizationEvent;
import com.chisimdi.payment.events.LedgerPaymentEvent;
import com.chisimdi.payment.events.RefundCompletedEvent;
import com.chisimdi.payment.events.SettlementEvent;
import com.chisimidi.account.service.mappers.VaultMapper;
import com.chisimidi.account.service.models.Account;
import com.chisimidi.account.service.models.BankAccount;
import com.chisimidi.account.service.repositories.AccountRepository;
import com.chisimidi.account.service.repositories.EventsIdempotencyRepository;
import com.chisimidi.account.service.repositories.VaultRepository;
import com.chisimidi.account.service.services.AccountEncoderService;
import com.chisimidi.account.service.services.VaultService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VaultServiceTest {
    @Mock
    private VaultRepository vaultRepository;
    @Mock
    private VaultMapper vaultMapper;
    @Mock
    private AccountEncoderService accountEncoderService;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private KafkaTemplate<String,Object> kafkaTemplate;
    @Mock
    private EventsIdempotencyRepository idempotencyRepository;
    @InjectMocks
    private VaultService vaultService;

    @Test
    void reserveFundsTest(){
        String token="abc";
        BigDecimal amount=BigDecimal.valueOf(2000);
        Account account=new BankAccount();
        account.setMoneyRemaining(BigDecimal.valueOf(2000));

        when(accountRepository.findById(accountEncoderService.decode(token))).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        vaultService.reserveFunds(token,amount);

        assertThat(account.getMoneyRemaining()).isEqualTo(BigDecimal.ZERO);

        verify(accountRepository).findById(accountEncoderService.decode(token));
        verify(accountRepository).save(account);
    }

    @Test
    void settlementTest(){
        SettlementEvent settlementEvent=new SettlementEvent();
        settlementEvent.setTotalAmount(BigDecimal.valueOf(2000));
        settlementEvent.setAmount(BigDecimal.valueOf(2000));
        settlementEvent.setMerchantToken("abc");

        Account account=new BankAccount();
        account.setMoneyRemaining(BigDecimal.valueOf(2000));
        account.setPendingAccount(BigDecimal.valueOf(0));

        when(accountRepository.findById(accountEncoderService.decode(settlementEvent.getMerchantToken()))).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        vaultService.settleMerchant(settlementEvent);

        assertThat(account.getPendingAccount()).isEqualTo(BigDecimal.valueOf(2000));

        verify(accountRepository).findById(accountEncoderService.decode(settlementEvent.getMerchantToken()));
        verify(accountRepository).save(account);
    }

    @Test
    void releaseFundTest(){
        FailAuthorizationEvent authorizationEvent=new FailAuthorizationEvent();
        authorizationEvent.setCustomerAccount("Abc");
        authorizationEvent.setAmount(BigDecimal.valueOf(2000));

        Account account=new BankAccount();
        account.setMoneyRemaining(BigDecimal.valueOf(2000));
        account.setPendingAccount(BigDecimal.valueOf(0));

        when(accountRepository.findById(accountEncoderService.decode(authorizationEvent.getCustomerAccount()))).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        vaultService.releaseFunds(authorizationEvent);

        assertThat(account.getMoneyRemaining()).isEqualTo(BigDecimal.valueOf(4000));

        verify(accountRepository).findById(accountEncoderService.decode(authorizationEvent.getCustomerAccount()));
        verify(accountRepository).save(account);
    }

    @Test
    void processRefundTest(){
        RefundCompletedEvent completedEvent=new RefundCompletedEvent();
        completedEvent.setAmount(BigDecimal.valueOf(2000));
        completedEvent.setCustomerAccountToken("ABc");

        Account account=new BankAccount();
        account.setMoneyRemaining(BigDecimal.valueOf(2000));
        account.setPendingAccount(BigDecimal.valueOf(0));

        when(accountRepository.findById(accountEncoderService.decode(completedEvent.getCustomerAccountToken()))).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        vaultService.processRefund(completedEvent);

        assertThat(account.getMoneyRemaining()).isEqualTo(BigDecimal.valueOf(4000));

        verify(accountRepository).findById(accountEncoderService.decode(completedEvent.getCustomerAccountToken()));
        verify(accountRepository).save(account);
    }

    @Test
    void payoutInitiatedTest(){

        LedgerPaymentEvent paymentEvent=new LedgerPaymentEvent();
        paymentEvent.setAccountTo("abc");
        paymentEvent.setAmount(BigDecimal.valueOf(2000));

        Account account=new BankAccount();
        account.setMoneyRemaining(BigDecimal.valueOf(2000));
        account.setPendingAccount(BigDecimal.valueOf(2000));

        when(accountRepository.findById(accountEncoderService.decode(paymentEvent.getAccountTo()))).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        vaultService.initiatePayout(paymentEvent);

        assertThat(account.getMoneyRemaining()).isEqualTo(BigDecimal.valueOf(4000));
        assertThat(account.getPendingAccount()).isEqualTo(BigDecimal.valueOf(0));

    }

}
