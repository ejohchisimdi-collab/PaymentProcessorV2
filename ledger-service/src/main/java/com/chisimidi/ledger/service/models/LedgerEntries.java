package com.chisimidi.ledger.service.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
public class LedgerEntries {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    private Ledger ledger;
    @Enumerated(EnumType.STRING)
    private EntryType creditEntry;
    @Enumerated(EnumType.STRING)
    private EntryType debitEntry;
    private BigDecimal creditAmount;
    private BigDecimal debitAmount;
    @Enumerated(EnumType.STRING)
    private EntryReason entryReason;
    private String accountTo;
    private String accountFrom;
    private Instant createdAt;

}
