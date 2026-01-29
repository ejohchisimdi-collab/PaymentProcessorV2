package com.chisimidi.ledger.service.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class EventIdempotency {
    @Id
    private String id;
    private String context;
    private LocalDateTime localDateTime=LocalDateTime.now();

}
