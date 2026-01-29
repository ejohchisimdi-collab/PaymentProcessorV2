package com.chisimidi.payment.service.services;

import com.chisimidi.payment.service.exceptions.FallBackException;
import com.chisimidi.payment.service.utils.AccountType;
import com.chisimidi.payment.service.utils.CaptureType;
import com.chisimidi.payment.service.utils.RefundType;
import com.chisimidi.payment.service.utils.ReserveFundsUtil;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.persistence.OptimisticLockException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Slf4j
@AllArgsConstructor
@Service
public class RestClientService {
    @Qualifier("userClient")
    private RestClient userClient;
    @Qualifier("accountClient")
    private RestClient accountClient;

    @CircuitBreaker(name = "userService", fallbackMethod = "doesMerchantExistFallBack")
    public Boolean doesMerchantExist(int userId){
        return userClient.get().uri("/users/{userId}/exists",userId).retrieve().body(Boolean.class);
    }
    public Boolean doesMerchantExistFallBack(int userId,Throwable t){
        throw new FallBackException("User profiles not available");
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "doesMerchantSettingExistFallBack")
    public Boolean doesMerchantSettingExist(int userId){
        return userClient.get().uri("/settings/{userId}/exists",userId).retrieve().body(Boolean.class);
    }

    public Boolean doesMerchantSettingExistFallBack(int userId,Throwable t){
        throw new FallBackException("User profiles not available");
    }


    @CircuitBreaker(name = "userService",fallbackMethod = "getMerchantCurrencyFallback")
    public String getMerchantCurrency(int userId){
        return userClient.get().uri("/settings/{userId}/currency",userId).retrieve().body(String.class);
    }

    public String getMerchantCurrencyFallback(int userId,Throwable t){
        throw new FallBackException("User profiles not available");
    }


    @CircuitBreaker(name = "userService",fallbackMethod = "transactionLimitFallBack")
    public BigDecimal getTransactionLimit(int userId){
        return userClient.get().uri("/settings/{userId}/transaction-limit",userId).retrieve().body(BigDecimal.class);
    }

    public BigDecimal transactionLimitFallBack(int userId,Throwable t){
        throw new FallBackException("User profiles not available");
    }

    @CircuitBreaker(name = "userService",fallbackMethod = "captureTypeFallBack")
    public CaptureType getCaptureType(int userId){
        return userClient.get().uri("/settings/{userId}/capture-type",userId).retrieve().body(CaptureType.class);
    }

    public CaptureType captureTypeFallBack(int userId,Throwable t){
        throw new FallBackException("User profiles not available");
    }

    @CircuitBreaker(name = "userService",fallbackMethod = "merchantSecretFallBack")
    public String getMerchantSecret(int userId){
        return userClient.get().uri("/settings/{userId}/secret",userId).retrieve().body(String.class);
    }

    public String merchantSecretFallBack(int userId,Throwable t){
        log.error(t.getMessage());
        throw new FallBackException("Hmac Secrets not available");
    }

    public RefundType getMerchantRefundType(int userId){
        return userClient.get().uri("/settings/{userId}/refund-type",userId).retrieve().body(RefundType.class);
    }
    @CircuitBreaker(name = "userService",fallbackMethod = "refundTypeFallBack")
    public RefundType refundTypeFallBack(int userId,Throwable t){
        throw new FallBackException("user profiles not available");
    }

    @CircuitBreaker(name = "accountService",fallbackMethod = "balanceFallBack")
    public BigDecimal getCustomerBalance(String token){
        return accountClient.get().uri("/accounts/{token}/balance", token).retrieve().body(BigDecimal.class);
    }

    public BigDecimal balanceFallBack(String token,Throwable t){
        throw new FallBackException("User accounts not available");
    }


    @CircuitBreaker(name = "accountService",fallbackMethod = "doesAccountExistFallBack")
    public Boolean doesCustomerAccountExist(String token){
        return accountClient.get().uri("/accounts/customer/{token}/exists",token).retrieve().body(Boolean.class);
    }

    public Boolean doesAccountExistFallBack(String token){
        throw new FallBackException("User accounts not available");
    }

    @CircuitBreaker(name = "accountService",fallbackMethod = "doesMerchantAccountExistFallBack")
    public Boolean doesMerchantAndAccountExist(String token,int id){
        return accountClient.get().uri("/accounts/merchant/token/{token}/id/{id}/exists",token,id).retrieve().body(Boolean.class);
    }

    public Boolean doesMerchantAccountExistFallBack(String token,int id,Throwable t){
        throw new FallBackException("User accounts not available");
    }

    @CircuitBreaker(name = "accountService",fallbackMethod = "getCustomerCurrencyFallback")
    public String getCustomerAccountCurrency(String token){
        return accountClient.get().uri("/accounts/{token}/currency", token).retrieve().body(String.class);
    }

    public String getCustomerCurrencyFallback(String token,Throwable t){
        throw new FallBackException("User accounts not available");
    }

    @CircuitBreaker(name = "accountService",fallbackMethod = "getAccountTypeFallBack")
    public AccountType getCustomerAccountType(String token){
        return accountClient.get().uri("/accounts/{token}/account-type", token).retrieve().body(AccountType.class);
    }

    public AccountType getAccountTypeFallBack(String token,Throwable t){
        throw new FallBackException("User accounts not available");
    }

    @Retryable(retryFor = OptimisticLockException.class,maxAttempts = 3,backoff = @Backoff(delay = 2,multiplier = 3))
    @CircuitBreaker(name = "accountService",fallbackMethod = "reserveFundsFallBack")
    public void reserveFunds(String token,BigDecimal amount){
        ReserveFundsUtil reserveFundsUtil=new ReserveFundsUtil();
        reserveFundsUtil.setAmount(amount);
        reserveFundsUtil.setToken(token);
        accountClient.post().uri("/reserve-funds").body(reserveFundsUtil).retrieve().toBodilessEntity();
    }
    public void reserveFundsFallBack(String token,BigDecimal amount,Throwable t){
        throw new FallBackException("User accounts not available");
    }







}
