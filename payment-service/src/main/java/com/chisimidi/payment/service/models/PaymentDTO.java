package com.chisimidi.payment.service.models;

import com.chisimidi.payment.service.utils.AccountType;
import jakarta.persistence.AccessType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PaymentDTO {
    private int id;
    private LocalDateTime createdAt;
    private LocalDateTime authorizationDueDate;
    private String currency;
    private String customerAccount;
    private String merchantAccount;
    private int merchantId;
    private AccountType accountType;
    private BigDecimal amount;
    private BigDecimal conversionFee;
    private BigDecimal platformFee;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    private List<String> warnings=new ArrayList<>();
    private BigDecimal amountAfterConversion;
}
