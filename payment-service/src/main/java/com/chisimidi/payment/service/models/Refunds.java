package com.chisimidi.payment.service.models;

import com.chisimidi.payment.service.utils.RefundType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Refunds {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private BigDecimal amount;
    private BigDecimal amountAfterConversion;
    @Enumerated(EnumType.STRING)
    private RefundType refundType;
    @ManyToOne
    private Payment payment;
    @Enumerated(EnumType.STRING)
    private RefundStatus refundStatus;
    private String merchantAccount;
    private String customerAccount;
    private int merchantId;
    private String currency;
    private BigDecimal platformFee;
    private BigDecimal conversionFee;
    private List<String>warnings=new ArrayList<>();

}
