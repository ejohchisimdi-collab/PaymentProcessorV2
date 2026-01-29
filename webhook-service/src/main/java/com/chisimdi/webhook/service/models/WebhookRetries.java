package com.chisimdi.webhook.service.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
public class WebhookRetries {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int paymentId;
    private String accountTo;
    private String accountFrom;
    private int merchantId;
    private BigDecimal amount;
    private int maxRetries=7;
    private int retryCount;
    private Boolean done=false;
    @Enumerated(EnumType.STRING)
    private Status status;
    private LocalDateTime nextRetryTime=LocalDateTime.now().plusSeconds(5);

}
