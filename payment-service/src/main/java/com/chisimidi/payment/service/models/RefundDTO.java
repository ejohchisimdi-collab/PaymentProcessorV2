package com.chisimidi.payment.service.models;

import com.chisimidi.payment.service.utils.RefundType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RefundDTO {
    private int id;
    private BigDecimal amount;
    private BigDecimal amountAfterConversion;
    private RefundType refundType;
    private int paymentId;
    private String merchantAccount;
    private String customerAccount;
    private int merchantId;
    private RefundStatus refundStatus;
    private BigDecimal platformFee;
    private BigDecimal conversionFee;
    private List<String> warnings=new ArrayList<>();
}
