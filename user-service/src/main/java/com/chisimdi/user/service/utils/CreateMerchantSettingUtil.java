package com.chisimdi.user.service.utils;

import com.chisimdi.user.service.models.CaptureType;
import com.chisimdi.user.service.models.RefundType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class CreateMerchantSettingUtil {
    @NotNull
    private String currency;
    @NotNull
    private String merchantEndpoint;
    @NotNull
    private BigDecimal maxTransactionLimit;
    @NotNull
    @Enumerated(EnumType.STRING)
    private CaptureType captureType;
    @NotNull
    @Enumerated(EnumType.STRING)
    private RefundType refundType;
    @Positive
    private int merchantId;

    public void setMerchantEndpoint(String merchantEndpoint) {
        this.merchantEndpoint = merchantEndpoint;
    }

    public String getMerchantEndpoint() {
        return merchantEndpoint;
    }

    public void setRefundType(RefundType refundType) {
        this.refundType = refundType;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrency() {
        return currency;
    }

    public RefundType getRefundType() {
        return refundType;
    }

    public BigDecimal getMaxTransactionLimit() {
        return maxTransactionLimit;
    }

    public CaptureType getCaptureType() {
        return captureType;
    }

    public void setCaptureType(CaptureType captureType) {
        this.captureType = captureType;
    }

    public void setMaxTransactionLimit(BigDecimal maxTransactionLimit) {
        this.maxTransactionLimit = maxTransactionLimit;
    }

    public int getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }
}
