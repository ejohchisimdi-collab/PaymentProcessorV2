package com.chisimidi.payment.service.models;

import com.chisimidi.payment.service.utils.AccountType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private LocalDateTime createdAt=LocalDateTime.now();
    private LocalDateTime authorizationDueDate;
    private String currency;
    private String customerAccount;
    private String merchantAccount;
    private int merchantId;
    private BigDecimal amount;
    private BigDecimal amountAfterConversion;
    private BigDecimal amountLeftToRefund;
    @Enumerated(EnumType.STRING)
    private AccountType accountType;
    private BigDecimal conversionFee;
    private BigDecimal platformFee;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;
    private Boolean done=false;
    private List<String>warnings=new ArrayList<>();
}
