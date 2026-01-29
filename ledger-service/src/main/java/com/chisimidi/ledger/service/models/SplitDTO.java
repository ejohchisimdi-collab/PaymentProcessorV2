package com.chisimidi.ledger.service.models;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
@Getter
@Setter
public class SplitDTO {
    private int id;
    private EntryType firstCreditEntry;
    private EntryType firstDebitEntry;
    private EntryType secondCreditEntry;
    private BigDecimal firstCreditAmount;
    private BigDecimal secondCreditAmount;
    private BigDecimal firstDebitAmount;
    private String accountTo;
    private String accountFrom;
    private Instant createdAt;
    private EntryReason entryReason;
}
