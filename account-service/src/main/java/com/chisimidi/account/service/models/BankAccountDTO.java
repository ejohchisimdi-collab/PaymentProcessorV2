package com.chisimidi.account.service.models;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
public class BankAccountDTO {
    private String accountNumber;
    private int userId;
    private OwnerType ownerType;
    private String currency;
    private BigDecimal balance;
    BigDecimal pendingAccount;
}
