package com.chisimidi.account.service.services;

import com.chisimidi.account.service.excptions.ResourceNotFoundException;
import com.chisimidi.account.service.mappers.BankAccountMapper;
import com.chisimidi.account.service.mappers.CreditCardMapper;
import com.chisimidi.account.service.models.*;
import com.chisimidi.account.service.repositories.BankAccountRepository;
import com.chisimidi.account.service.repositories.CreditCardRepository;
import com.chisimidi.account.service.repositories.VaultRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class AccountService {
    private BankAccountRepository bankAccountRepository;
    private CreditCardRepository creditCardRepository;
    private CreditCardMapper creditCardMapper;
    private VaultRepository vaultRepository;
    private BankAccountMapper bankAccountMapper;
    private AccountEncoderService accountEncoderService;
    private RestClientService restClientService;

    @Transactional
    public BankAccountDTO createMerchantBankAccount(String accountNumber,int userId,String currency,BigDecimal balance){
        CurrencyUnit currencyUnit= Monetary.getCurrency(currency);
    if(!restClientService.doesUserExist(userId)){
    throw new ResourceNotFoundException("User with id "+userId+" not found");
    }

        BankAccount bankAccount=new BankAccount();
        bankAccount.setAccountNumber(accountNumber);
        bankAccount.setMoneyRemaining(balance);
        bankAccount.setOwnerType(OwnerType.MERCHANT);
        bankAccount.setCurrency(currency);
        bankAccount.setUserId(userId);
        bankAccountRepository.save(bankAccount);

        Vault vault=new Vault();
        vault.setAccountType(AccountType.BANK);
        vault.setToken(accountEncoderService.encode(accountNumber));
        vault.setCurrency(currency);
        vault.setOwnerType(OwnerType.MERCHANT);
        vault.setLast4digits(accountNumber.substring(accountNumber.length()-4));
        vaultRepository.save(vault);

        return bankAccountMapper.toBankAccountDTO(bankAccount);
    }
    @Transactional
    public BankAccountDTO createCustomerBankAccount(String accountNumber,int userId,String currency,BigDecimal balance){
        CurrencyUnit currencyUnit= Monetary.getCurrency(currency);
        if(!restClientService.doesUserExist(userId)){
            throw new ResourceNotFoundException("User with id "+userId+" not found");
        }

        BankAccount bankAccount=new BankAccount();
        bankAccount.setAccountNumber(accountNumber);
        bankAccount.setMoneyRemaining(balance);
        bankAccount.setOwnerType(OwnerType.CUSTOMER);
        bankAccount.setCurrency(currency);
        bankAccount.setUserId(userId);
        bankAccountRepository.save(bankAccount);

        Vault vault=new Vault();
        vault.setAccountType(AccountType.BANK);
        vault.setToken(accountEncoderService.encode(accountNumber));
        vault.setCurrency(currency);
        vault.setOwnerType(OwnerType.CUSTOMER);
        vault.setLast4digits(accountNumber.substring(accountNumber.length()-4));
        vaultRepository.save(vault);

        return bankAccountMapper.toBankAccountDTO(bankAccount);
    }

    @Transactional
    public CreditCardDTO createMerchantCreditCard(String accountNumber,int userId,String currency,BigDecimal creditLimit){
        CurrencyUnit currencyUnit= Monetary.getCurrency(currency);
        if(!restClientService.doesUserExist(userId)){
            throw new ResourceNotFoundException("User with id "+userId+" not found");
        }
        CreditCard creditCard=new CreditCard();
        creditCard.setCreditLimit(creditLimit);
        creditCard.setAccountNumber(accountNumber);
        creditCard.setMoneyRemaining(creditLimit);
        creditCard.setCurrency(currency);
        creditCard.setOwnerType(OwnerType.MERCHANT);
        creditCard.setUserId(userId);
        creditCardRepository.save(creditCard);

        Vault vault=new Vault();
        vault.setAccountType(AccountType.CREDIT);
        vault.setToken(accountEncoderService.encode(accountNumber));
        vault.setCurrency(currency);
        vault.setOwnerType(OwnerType.MERCHANT);
        vault.setLast4digits(accountNumber.substring(accountNumber.length()-4));
        vaultRepository.save(vault);

        return creditCardMapper.toCreditCardDTO(creditCard);
    }

    @Transactional
    public CreditCardDTO createCustomerCreditCard(String accountNumber,int userId,String currency,BigDecimal creditLimit){
        CurrencyUnit currencyUnit= Monetary.getCurrency(currency);
        if(!restClientService.doesUserExist(userId)){
            throw new ResourceNotFoundException("User with id "+userId+" not found");
        }
        CreditCard creditCard=new CreditCard();
        creditCard.setCreditLimit(creditLimit);
        creditCard.setAccountNumber(accountNumber);
        creditCard.setMoneyRemaining(creditLimit);
        creditCard.setCurrency(currency);
        creditCard.setOwnerType(OwnerType.CUSTOMER);
        creditCard.setUserId(userId);
        creditCardRepository.save(creditCard);

        Vault vault=new Vault();
        vault.setAccountType(AccountType.CREDIT);
        vault.setToken(accountEncoderService.encode(accountNumber));
        vault.setCurrency(currency);
        vault.setOwnerType(OwnerType.CUSTOMER);
        vault.setLast4digits(accountNumber.substring(accountNumber.length()-4));
        vaultRepository.save(vault);

        return creditCardMapper.toCreditCardDTO(creditCard);
    }

    public List<CreditCardDTO>findAllCreditCardsByUser(int userId, int pageNumber, int size){
        Page<CreditCard>creditCards=creditCardRepository.findByUserId(userId, PageRequest.of(pageNumber,size));
        return creditCardMapper.toCreditCardDTOList(creditCards.getContent());
    }

    public List<BankAccountDTO>findAllBankAccountsByUser(int userId,int pageNumber,int size){
        Page<BankAccount>bankAccounts=bankAccountRepository.findByUserId(userId, PageRequest.of(pageNumber,size));
        return bankAccountMapper.toBankAccountDTOList(bankAccounts.getContent());
    }


}
