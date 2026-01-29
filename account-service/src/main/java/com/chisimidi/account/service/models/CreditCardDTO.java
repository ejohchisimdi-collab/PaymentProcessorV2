package com.chisimidi.account.service.models;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
public class CreditCardDTO  {
    private String accountNumber;
    private int userId;
    private OwnerType ownerType;
    private String currency;
    private BigDecimal creditLimit;
    private BigDecimal totalCreditRemaining;
    BigDecimal pendingAccount;
}
