package com.chisimdi.payment.events;

import java.math.BigDecimal;

public class AuthorizationEvent {
    private String id;
    private int paymentId;
    private String accountTo;
    private String accountFrom;
    private int merchantId;
    private BigDecimal amount;

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public void setAccountTo(String accountTo) {
        this.accountTo = accountTo;
    }

    public void setAccountFrom(String accountFrom) {
        this.accountFrom = accountFrom;
    }

    public String getAccountTo() {
        return accountTo;
    }

    public String getAccountFrom() {
        return accountFrom;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }

    public int getMerchantId() {
        return merchantId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
