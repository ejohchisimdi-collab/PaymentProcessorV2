package com.chisimidi.account.service;

import com.chisimidi.account.service.excptions.ResourceNotFoundException;
import com.chisimidi.account.service.mappers.BankAccountMapper;
import com.chisimidi.account.service.mappers.CreditCardMapper;
import com.chisimidi.account.service.models.*;
import com.chisimidi.account.service.repositories.BankAccountRepository;
import com.chisimidi.account.service.repositories.CreditCardRepository;
import com.chisimidi.account.service.repositories.VaultRepository;
import com.chisimidi.account.service.services.AccountEncoderService;
import com.chisimidi.account.service.services.AccountService;
import com.chisimidi.account.service.services.RestClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.money.UnknownCurrencyException;
import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
    @Mock
    private BankAccountRepository bankAccountRepository;
    @Mock
    private CreditCardRepository creditCardRepository;
    @Mock
    private CreditCardMapper creditCardMapper;
    @Mock
    private VaultRepository vaultRepository;
    @Mock
    private BankAccountMapper bankAccountMapper;
    @Mock
    private AccountEncoderService accountEncoderService;
    @Mock
    private RestClientService restClientService;
    @InjectMocks
    private AccountService accountService;


    @Test
    void createMerchantBankAccountTest(){
        String accountNumber="123-456-789";
        int userId=1;
        String currency="USD";
        BankAccount bankAccount=new BankAccount();
        Vault vault=new Vault();
        BigDecimal balance=BigDecimal.valueOf(200);
        ArgumentCaptor<BankAccount>bankAccountArgumentCaptor=ArgumentCaptor.forClass(BankAccount.class);
        ArgumentCaptor<Vault>vaultArgumentCaptor=ArgumentCaptor.forClass(Vault.class);

        when(restClientService.doesUserExist(userId)).thenReturn(true);
        when(bankAccountRepository.save(any(BankAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(vaultRepository.save(any(Vault.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        accountService.createMerchantBankAccount(accountNumber,userId,currency,balance);

        verify(bankAccountRepository).save(bankAccountArgumentCaptor.capture());
        verify(vaultRepository).save(vaultArgumentCaptor.capture());

        bankAccount=bankAccountArgumentCaptor.getValue();
        vault=vaultArgumentCaptor.getValue();

        assertThat(bankAccount.getOwnerType()).isEqualTo(OwnerType.MERCHANT);
        assertThat(vault.getAccountType()).isEqualTo(AccountType.BANK);
        assertThat(vault.getOwnerType()).isEqualTo(OwnerType.MERCHANT);

        verify(restClientService).doesUserExist(userId);
    }

    @Test
    void createMerchantBankAccountTest_ThrowsUnknownCurrencyException(){
        String accountNumber="123-456-789";
        int userId=1;
        String currency="123";
        BankAccount bankAccount=new BankAccount();
        Vault vault=new Vault();
        BigDecimal balance=BigDecimal.valueOf(200);

        assertThatThrownBy(()->accountService.createMerchantBankAccount(accountNumber,userId,currency,balance)).isInstanceOf(UnknownCurrencyException.class);

    }

    @Test
    void createMerchantBankAccountTest_ThrowsResourceNotFoundExceptionForUser(){
        String accountNumber="123-456-789";
        int userId=1;
        String currency="USD";
        BankAccount bankAccount=new BankAccount();
        Vault vault=new Vault();
        BigDecimal balance=BigDecimal.valueOf(200);
        ArgumentCaptor<BankAccount>bankAccountArgumentCaptor=ArgumentCaptor.forClass(BankAccount.class);
        ArgumentCaptor<Vault>vaultArgumentCaptor=ArgumentCaptor.forClass(Vault.class);

        when(restClientService.doesUserExist(userId)).thenReturn(false);

        assertThatThrownBy(()->accountService.createMerchantBankAccount(accountNumber,userId,currency,balance)).isInstanceOf(ResourceNotFoundException.class);

        verify(restClientService).doesUserExist(userId);
    }

    @Test
    void createCustomerBankAccountTest(){
        String accountNumber="123-456-789";
        int userId=1;
        String currency="USD";
        BankAccount bankAccount=new BankAccount();
        Vault vault=new Vault();
        BigDecimal balance=BigDecimal.valueOf(200);
        ArgumentCaptor<BankAccount>bankAccountArgumentCaptor=ArgumentCaptor.forClass(BankAccount.class);
        ArgumentCaptor<Vault>vaultArgumentCaptor=ArgumentCaptor.forClass(Vault.class);

        when(restClientService.doesUserExist(userId)).thenReturn(true);
        when(bankAccountRepository.save(any(BankAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(vaultRepository.save(any(Vault.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        accountService.createCustomerBankAccount(accountNumber,userId,currency,balance);

        verify(bankAccountRepository).save(bankAccountArgumentCaptor.capture());
        verify(vaultRepository).save(vaultArgumentCaptor.capture());

        bankAccount=bankAccountArgumentCaptor.getValue();
        vault=vaultArgumentCaptor.getValue();

        assertThat(bankAccount.getOwnerType()).isEqualTo(OwnerType.CUSTOMER);
        assertThat(vault.getAccountType()).isEqualTo(AccountType.BANK);
        assertThat(vault.getOwnerType()).isEqualTo(OwnerType.CUSTOMER);

        verify(restClientService).doesUserExist(userId);
    }

    @Test
    void createCustomerBankAccountTest_ThrowsUnknownCurrencyException(){
        String accountNumber="123-456-789";
        int userId=1;
        String currency="123";
        BankAccount bankAccount=new BankAccount();
        Vault vault=new Vault();
        BigDecimal balance=BigDecimal.valueOf(200);

        assertThatThrownBy(()->accountService.createCustomerBankAccount(accountNumber,userId,currency,balance)).isInstanceOf(UnknownCurrencyException.class);

    }

    @Test
    void createCustomerBankAccountTest_ThrowsResourceNotFoundExceptionForUser(){
        String accountNumber="123-456-789";
        int userId=1;
        String currency="USD";
        BankAccount bankAccount=new BankAccount();
        Vault vault=new Vault();
        BigDecimal balance=BigDecimal.valueOf(200);
        ArgumentCaptor<BankAccount>bankAccountArgumentCaptor=ArgumentCaptor.forClass(BankAccount.class);
        ArgumentCaptor<Vault>vaultArgumentCaptor=ArgumentCaptor.forClass(Vault.class);

        when(restClientService.doesUserExist(userId)).thenReturn(false);

        assertThatThrownBy(()->accountService.createCustomerBankAccount(accountNumber,userId,currency,balance)).isInstanceOf(ResourceNotFoundException.class);

        verify(restClientService).doesUserExist(userId);
    }

    @Test
    void createMerchantCreditCardTest(){
        String accountNumber="123-456-789";
        int userId=1;
        String currency="USD";
        CreditCard creditCard=new CreditCard();
        Vault vault=new Vault();
        BigDecimal balance=BigDecimal.valueOf(200);
        ArgumentCaptor<CreditCard> creditCardArgumentCaptor =ArgumentCaptor.forClass(CreditCard.class);
        ArgumentCaptor<Vault>vaultArgumentCaptor=ArgumentCaptor.forClass(Vault.class);

        when(restClientService.doesUserExist(userId)).thenReturn(true);
        when(creditCardRepository.save(any(CreditCard.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(vaultRepository.save(any(Vault.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        accountService.createMerchantCreditCard(accountNumber,userId,currency,balance);

        verify(creditCardRepository).save(creditCardArgumentCaptor.capture());
        verify(vaultRepository).save(vaultArgumentCaptor.capture());

        creditCard= creditCardArgumentCaptor.getValue();
        vault=vaultArgumentCaptor.getValue();

        assertThat(creditCard.getOwnerType()).isEqualTo(OwnerType.MERCHANT);
        assertThat(vault.getAccountType()).isEqualTo(AccountType.CREDIT);
        assertThat(vault.getOwnerType()).isEqualTo(OwnerType.MERCHANT);

        verify(restClientService).doesUserExist(userId);
    }

    @Test
    void createMerchantCreditCardTest_ThrowsUnknownCurrencyException(){
        String accountNumber="123-456-789";
        int userId=1;
        String currency="123";
        BankAccount bankAccount=new BankAccount();
        Vault vault=new Vault();
        BigDecimal balance=BigDecimal.valueOf(200);

        assertThatThrownBy(()->accountService.createMerchantCreditCard(accountNumber,userId,currency,balance)).isInstanceOf(UnknownCurrencyException.class);

    }

    @Test
    void createMerchantCreditCardTest_ThrowsResourceNotFoundExceptionForUser(){
        String accountNumber="123-456-789";
        int userId=1;
        String currency="USD";
        BankAccount bankAccount=new BankAccount();
        Vault vault=new Vault();
        BigDecimal balance=BigDecimal.valueOf(200);
        ArgumentCaptor<BankAccount>bankAccountArgumentCaptor=ArgumentCaptor.forClass(BankAccount.class);
        ArgumentCaptor<Vault>vaultArgumentCaptor=ArgumentCaptor.forClass(Vault.class);

        when(restClientService.doesUserExist(userId)).thenReturn(false);

        assertThatThrownBy(()->accountService.createMerchantCreditCard(accountNumber,userId,currency,balance)).isInstanceOf(ResourceNotFoundException.class);

        verify(restClientService).doesUserExist(userId);
    }

    @Test
    void createCustomerCreditCardTest(){
        String accountNumber="123-456-789";
        int userId=1;
        String currency="USD";
        CreditCard creditCard=new CreditCard();
        Vault vault=new Vault();
        BigDecimal balance=BigDecimal.valueOf(200);
        ArgumentCaptor<CreditCard> creditCardArgumentCaptor =ArgumentCaptor.forClass(CreditCard.class);
        ArgumentCaptor<Vault>vaultArgumentCaptor=ArgumentCaptor.forClass(Vault.class);

        when(restClientService.doesUserExist(userId)).thenReturn(true);
        when(creditCardRepository.save(any(CreditCard.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(vaultRepository.save(any(Vault.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        accountService.createCustomerCreditCard(accountNumber,userId,currency,balance);

        verify(creditCardRepository).save(creditCardArgumentCaptor.capture());
        verify(vaultRepository).save(vaultArgumentCaptor.capture());

        creditCard= creditCardArgumentCaptor.getValue();
        vault=vaultArgumentCaptor.getValue();

        assertThat(creditCard.getOwnerType()).isEqualTo(OwnerType.CUSTOMER);
        assertThat(vault.getAccountType()).isEqualTo(AccountType.CREDIT);
        assertThat(vault.getOwnerType()).isEqualTo(OwnerType.CUSTOMER);

        verify(restClientService).doesUserExist(userId);
    }

    @Test
    void createCustomerCreditCardTest_ThrowsUnknownCurrencyException(){
        String accountNumber="123-456-789";
        int userId=1;
        String currency="123";
        BankAccount bankAccount=new BankAccount();
        Vault vault=new Vault();
        BigDecimal balance=BigDecimal.valueOf(200);

        assertThatThrownBy(()->accountService.createCustomerCreditCard(accountNumber,userId,currency,balance)).isInstanceOf(UnknownCurrencyException.class);

    }

    @Test
    void createCustomerCreditCardTest_ThrowsResourceNotFoundExceptionForUser(){
        String accountNumber="123-456-789";
        int userId=1;
        String currency="USD";
        BankAccount bankAccount=new BankAccount();
        Vault vault=new Vault();
        BigDecimal balance=BigDecimal.valueOf(200);
        ArgumentCaptor<BankAccount>bankAccountArgumentCaptor=ArgumentCaptor.forClass(BankAccount.class);
        ArgumentCaptor<Vault>vaultArgumentCaptor=ArgumentCaptor.forClass(Vault.class);

        when(restClientService.doesUserExist(userId)).thenReturn(false);

        assertThatThrownBy(()->accountService.createCustomerCreditCard(accountNumber,userId,currency,balance)).isInstanceOf(ResourceNotFoundException.class);

        verify(restClientService).doesUserExist(userId);
    }





}
