package com.chisimidi.ledger.service.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Split {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    private Ledger ledger;
    @Enumerated(EnumType.STRING)
    private EntryType firstCreditEntry;
    @Enumerated(EnumType.STRING)
    private EntryType firstDebitEntry;
    @Enumerated(EnumType.STRING)
    private EntryType secondCreditEntry;
    private BigDecimal firstCreditAmount;
    private BigDecimal secondCreditAmount;
    private BigDecimal firstDebitAmount;
    private String accountTo;
    private String accountFrom;
    private Instant createdAt;
    @Enumerated(EnumType.STRING)
    private EntryReason entryReason;
    private Instant maturingDate;
    Boolean done=false;
}
