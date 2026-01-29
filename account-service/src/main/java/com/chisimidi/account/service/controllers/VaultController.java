package com.chisimidi.account.service.controllers;

import com.chisimidi.account.service.models.AccountType;
import com.chisimidi.account.service.models.VaultDTO;
import com.chisimidi.account.service.services.VaultService;
import com.chisimidi.account.service.utils.ReserveFundsUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@RestController
public class VaultController {
    private VaultService vaultService;

    @Operation(summary = "Retrieves all vaulted accounts, available only to admins")
    @PreAuthorize("hasRole('ROLE_Admin')")
    @GetMapping("/vaults/")
    public List<VaultDTO>getAllVaults(@RequestParam(defaultValue = "0")int pageNumber,@RequestParam(defaultValue = "10")int size){
        return vaultService.findAllVaults(pageNumber, size);
    }

    @Operation(summary = "Retrieves account balance, available only to services")
    @PreAuthorize("hasRole('ROLE_Service')")
    @GetMapping("/accounts/{token}/balance")
    public BigDecimal findAccountBalance(@PathVariable("token")String token){
        return vaultService.findAccountBalance(token);
    }

    @Operation(summary = "Verifies if a customer account exists, available only to services")
    @PreAuthorize("hasRole('ROLE_Service')")
    @GetMapping("/accounts/customer/{token}/exists")
    public Boolean doesCustomerAccountExists(@PathVariable("token")String token){
        return vaultService.doesCustomerAccountExist(token);
    }

    @Operation(summary = "Verifies if a merchant account exists, available only to services")
    @PreAuthorize("hasRole('ROLE_Service')")
    @GetMapping("/accounts/merchant/token/{token}/id/{merchantId}/exists")
    public Boolean doesMerchantAccountExists(@PathVariable("token")String token,@PathVariable("merchantId")int merchantId){
        return vaultService.doesMerchantAccountExist(token,merchantId);
    }

    @Operation(summary = "Retrieves the currency of an account, available only to services")
    @PreAuthorize("hasRole('ROLE_Service')")
    @GetMapping("/accounts/{token}/currency")
    public String findAccountCurrency(@PathVariable("token")String token){
        return vaultService.getAccountCurrency(token);
    }

    @Operation(summary = "Retrieves an accounts type, available only to services")
    @PreAuthorize("hasRole('ROLE_Service')")
    @GetMapping("/accounts/{token}/account-type")
    public AccountType findAccountType(@PathVariable("token")String token){
        return vaultService.getAccountType(token);
    }

    @Operation(summary = "Reserves funds of an account, available only to services")
    @PreAuthorize("hasRole('ROLE_Service')")
    @PostMapping("/reserve-funds")
    public void reserveFunds(@RequestBody ReserveFundsUtil fundsUtil){
        vaultService.reserveFunds(fundsUtil.getToken(),fundsUtil.getAmount());
    }



}
