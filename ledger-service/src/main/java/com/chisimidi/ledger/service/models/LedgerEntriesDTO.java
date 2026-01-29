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
public class LedgerEntriesDTO {
    private int id;
    private EntryType creditEntry;
    private EntryType debitEntry;
    private BigDecimal creditAmount;
    private BigDecimal debitAmount;
    private EntryReason entryReason;
    private String accountTo;
    private String accountFrom;
    private Instant createdAt;
}
