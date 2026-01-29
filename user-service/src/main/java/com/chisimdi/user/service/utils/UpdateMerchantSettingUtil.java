package com.chisimdi.user.service.utils;

import com.chisimdi.user.service.models.CaptureType;
import com.chisimdi.user.service.models.RefundType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;

public class UpdateMerchantSettingUtil {
    private String currency;
    private String merchantEndpoint;
    private BigDecimal maxTransactionLimit;
    @Enumerated(EnumType.STRING)
    private CaptureType captureType;
    @Enumerated(EnumType.STRING)
    private RefundType refundType;
    private int merchantId;

    public void setMaxTransactionLimit(BigDecimal maxTransactionLimit) {
        this.maxTransactionLimit = maxTransactionLimit;
    }

    public void setCaptureType(CaptureType captureType) {
        this.captureType = captureType;
    }

    public CaptureType getCaptureType() {
        return captureType;
    }

    public BigDecimal getMaxTransactionLimit() {
        return maxTransactionLimit;
    }

    public RefundType getRefundType() {
        return refundType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setRefundType(RefundType refundType) {
        this.refundType = refundType;
    }

    public String getMerchantEndpoint() {
        return merchantEndpoint;
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
}
