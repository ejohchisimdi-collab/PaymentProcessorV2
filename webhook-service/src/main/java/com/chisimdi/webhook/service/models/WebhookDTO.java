package com.chisimdi.webhook.service.models;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
public class WebhookDTO {
    private int paymentId;
    private String accountTo;
    private String accountFrom;
    private int merchantId;
    private BigDecimal amount;
    private Status status;

}
