package com.chisimidi.account.service.models;

import jakarta.persistence.Entity;

import java.math.BigDecimal;
@Entity
public class CreditCard extends Account {
   private BigDecimal creditLimit;
   private BigDecimal totalCreditRemaining;

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public BigDecimal getMoneyRemaining(){
        return totalCreditRemaining;
    }

    public void setMoneyRemaining(BigDecimal totalCreditRemaining) {
        this.totalCreditRemaining = totalCreditRemaining;
    }
}
