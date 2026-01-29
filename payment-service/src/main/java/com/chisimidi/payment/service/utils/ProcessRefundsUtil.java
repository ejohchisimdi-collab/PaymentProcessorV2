package com.chisimidi.payment.service.utils;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
public class ProcessRefundsUtil {
    @Positive
    private int merchantId;
    @Positive
    private int paymentId;
    @NotNull
    private BigDecimal amount;
}
