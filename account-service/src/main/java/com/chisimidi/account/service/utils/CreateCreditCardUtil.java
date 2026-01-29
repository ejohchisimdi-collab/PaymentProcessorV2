package com.chisimidi.account.service.utils;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
public class CreateCreditCardUtil {
    @NotNull
    @Size(min = 6,message = "Must be at least 6")
    private String accountNumber;
    @Positive
    private int userId;
    @NotNull
    private String currency;
    @NotNull
    private BigDecimal creditLimit;
}
