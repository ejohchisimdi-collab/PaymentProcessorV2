package com.chisimdi.payment.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public class CaptureEvent {
    String id;
    int merchantId;
    int paymentId;
    BigDecimal platformFee;
    String merchantAccount;
    String customerAccount;
    BigDecimal totalAmount;

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMerchantId() {
        return merchantId;
    }

    public void setCustomerAccount(String customerAccount) {
        this.customerAccount = customerAccount;
    }

    public String getCustomerAccount() {
        return customerAccount;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public String getId() {
        return id;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public BigDecimal getPlatformFee() {
        return platformFee;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }



    public String getMerchantAccount() {
        return merchantAccount;
    }



    public void setMerchantAccount(String merchantAccount) {
        this.merchantAccount = merchantAccount;
    }

    public void setPlatformFee(BigDecimal platformFee) {
        this.platformFee = platformFee;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

}
