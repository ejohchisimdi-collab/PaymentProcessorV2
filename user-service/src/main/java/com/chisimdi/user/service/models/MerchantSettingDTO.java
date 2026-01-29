package com.chisimdi.user.service.models;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;

import java.math.BigDecimal;

public class MerchantSettingDTO {
    private int id;
    private String currency;
    private String merchantEndpoint;
    private BigDecimal maxTransactionLimit;
    @Enumerated(EnumType.STRING)
    private CaptureType captureType;
    @Enumerated(EnumType.STRING)
    private RefundType refundType;
    private int merchantId;
    private String hmacSecret;

    public void setHmacSecret(String hmacSecret) {
        this.hmacSecret = hmacSecret;
    }

    public String getHmacSecret() {
        return hmacSecret;
    }

    public void setMaxTransactionLimit(BigDecimal maxTransactionLimit) {
        this.maxTransactionLimit = maxTransactionLimit;
    }

    public void setCaptureType(CaptureType captureType) {
        this.captureType = captureType;
    }

    public CaptureType getCaptureType() {
        return captureType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getMaxTransactionLimit() {
        return maxTransactionLimit;
    }

    public void setRefundType(RefundType refundType) {
        this.refundType = refundType;
    }

    public String getMerchantEndpoint() {
        return merchantEndpoint;
    }

    public RefundType getRefundType() {
        return refundType;
    }

    public void setMerchantEndpoint(String merchantEndpoint) {
        this.merchantEndpoint = merchantEndpoint;
    }

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }

    public int getMerchantId() {
        return merchantId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
