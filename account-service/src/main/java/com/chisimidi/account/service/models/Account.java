package com.chisimidi.account.service.models;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Account {
    @Id
    String accountNumber;
    int userId;
    @Enumerated(EnumType.STRING)
    OwnerType ownerType;
    String currency;
    BigDecimal pendingAccount=BigDecimal.valueOf(0);
    @Version
    int version;
    public abstract BigDecimal getMoneyRemaining();

     public abstract void setMoneyRemaining(BigDecimal bigDecimal);

     public String getAccountNumber(){
         return accountNumber;
     }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public OwnerType getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(OwnerType ownerType) {
        this.ownerType = ownerType;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrency() {
        return currency;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public BigDecimal getPendingAccount() {
        return pendingAccount;
    }

    public void setPendingAccount(BigDecimal pendingAccount) {
        this.pendingAccount = pendingAccount;
    }
}

