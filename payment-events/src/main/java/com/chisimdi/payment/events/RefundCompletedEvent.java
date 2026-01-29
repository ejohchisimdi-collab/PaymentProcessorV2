package com.chisimdi.payment.events;

import java.math.BigDecimal;

public class RefundCompletedEvent {
    private String id;
    private String customerAccountToken;
    private int merchantId;
    private int paymentId;
    private String merchantToken;
    private BigDecimal amount;
    private BigDecimal totalAmount;

    public int getMerchantId() {
        return merchantId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCustomerAccountToken() {
        return customerAccountToken;
    }

    public void setCustomerAccountToken(String customerAccountToken) {
        this.customerAccountToken = customerAccountToken;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setMerchantToken(String merchantToken) {
        this.merchantToken = merchantToken;
    }

    public String getMerchantToken() {
        return merchantToken;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
