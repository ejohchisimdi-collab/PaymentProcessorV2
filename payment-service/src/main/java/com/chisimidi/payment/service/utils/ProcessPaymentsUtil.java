package com.chisimidi.payment.service.utils;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
public class ProcessPaymentsUtil {
    @Positive
    private int merchantId;
    @NotNull
    private String merchantAccountToken;
    @NotNull
    private String customerAccountToken;
    @NotNull
    private BigDecimal amount;


}
