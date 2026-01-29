package com.chisimidi.account.service.controllers;

import com.chisimidi.account.service.models.BankAccountDTO;
import com.chisimidi.account.service.models.CreditCardDTO;
import com.chisimidi.account.service.services.AccountService;
import com.chisimidi.account.service.utils.CreateABankAccountUtil;
import com.chisimidi.account.service.utils.CreateCreditCardUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
public class AccountController {
    private AccountService accountService;

    @Operation(summary = "Create a merchant bank account, available only to merchants")
    @PreAuthorize("hasRole('ROLE_Merchant') and principal.userId == #bankAccountUtil.userId")
    @PostMapping("/merchants/bank-accounts/")
    public BankAccountDTO createAMerchantBankAccount(@Valid @RequestBody CreateABankAccountUtil bankAccountUtil){
        return accountService.createMerchantBankAccount(bankAccountUtil.getAccountNumber(), bankAccountUtil.getUserId(), bankAccountUtil.getCurrency(), bankAccountUtil.getBalance());
    }

    @Operation(summary = "Creates a customer bank account, available only to customers")
    @PreAuthorize("hasRole('ROLE_Customer') and principal.userId == #bankAccountUtil.userId")
    @PostMapping("/customers/bank-accounts/")
    public BankAccountDTO createACustomerBankAccount(@Valid @RequestBody CreateABankAccountUtil bankAccountUtil){
        return accountService.createCustomerBankAccount(bankAccountUtil.getAccountNumber(), bankAccountUtil.getUserId(), bankAccountUtil.getCurrency(), bankAccountUtil.getBalance());
    }

    @Operation(summary = "Creates a merchant credit card, available only to merchants")
    @PreAuthorize("hasRole('ROLE_Merchant') and principal.userId == #creditCardUtil.userId")
    @PostMapping("/merchants/credit-cards/")
    public CreditCardDTO createMerchantCreditCards(@Valid @RequestBody CreateCreditCardUtil creditCardUtil){
        return accountService.createMerchantCreditCard(creditCardUtil.getAccountNumber(), creditCardUtil.getUserId(), creditCardUtil.getCurrency(), creditCardUtil.getCreditLimit());
    }

    @Operation(summary = "Creates a customer credit card, available only to customers")
    @PreAuthorize("hasRole('ROLE_Customer') and principal.userId == #creditCardUtil.userId")
    @PostMapping("/customers/credit-cards/")
    public CreditCardDTO createCustomerCreditCards(@Valid @RequestBody CreateCreditCardUtil creditCardUtil){
        return accountService.createCustomerCreditCard(creditCardUtil.getAccountNumber(), creditCardUtil.getUserId(), creditCardUtil.getCurrency(), creditCardUtil.getCreditLimit());
    }

    @Operation(summary = "Retrieves all credit cards by user")
    @PreAuthorize("principal.userId == #userId")
    @GetMapping("/credit-cards/{userId}")
    public List<CreditCardDTO>findCreditCardsByUser(@PathVariable("userId")int userId,@RequestParam(defaultValue = "0")int pageNumber,@RequestParam(defaultValue = "10")int size){
        return accountService.findAllCreditCardsByUser(userId, pageNumber, size);
    }

    @Operation(summary = "Retrieves all bank accounts by user")
    @PreAuthorize("principal.userId == #userId")
    @GetMapping("/bank-accounts/{userId}")
    public List<BankAccountDTO>findBankAccountsAByUser(@PathVariable("userId")int userId,@RequestParam(defaultValue = "0")int pageNumber,@RequestParam(defaultValue = "10")int size){
        return accountService.findAllBankAccountsByUser(userId, pageNumber, size);
    }
}
