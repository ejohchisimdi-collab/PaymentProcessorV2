package com.chisimdi.user.service.models;

import jakarta.persistence.*;

import java.math.BigDecimal;
@Entity
public class MerchantSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String currency;
    private String merchantEndpoint;
    @Column(precision = 16,scale = 2)
    private BigDecimal maxTransactionLimit;
    @Enumerated(EnumType.STRING)
    private CaptureType captureType;
    @Enumerated(EnumType.STRING)
    private RefundType refundType;
    @OneToOne
    private User merchant;
    private String hmacSecret;


    public void setMerchantEndpoint(String merchantEndpoint) {
        this.merchantEndpoint = merchantEndpoint;
    }

    public User getMerchant() {
        return merchant;
    }

    public void setMerchant(User merchant) {
        this.merchant = merchant;
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

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getMaxTransactionLimit() {
        return maxTransactionLimit;
    }

    public CaptureType getCaptureType() {
        return captureType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCaptureType(CaptureType captureType) {
        this.captureType = captureType;
    }

    public void setMaxTransactionLimit(BigDecimal maxTransactionLimit) {
        this.maxTransactionLimit = maxTransactionLimit;
    }

    public String getHmacSecret() {
        return hmacSecret;
    }

    public void setHmacSecret(String hmacSecret) {
        this.hmacSecret = hmacSecret;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
