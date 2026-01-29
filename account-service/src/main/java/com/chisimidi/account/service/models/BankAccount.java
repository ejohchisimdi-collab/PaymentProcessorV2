package com.chisimidi.account.service.models;

import jakarta.persistence.Entity;

import java.math.BigDecimal;
@Entity
public class BankAccount extends Account {
    private BigDecimal balance;

    public BigDecimal getMoneyRemaining(){
        return balance;
    }
    public void setMoneyRemaining(BigDecimal bigDecimal){
        this.balance=bigDecimal;
    }
}
