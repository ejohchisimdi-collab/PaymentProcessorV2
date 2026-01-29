package com.chisimidi.account.service.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@Entity
public class EventsIdempotency {
    @Id
    private String id;
    private String context;
    private LocalDateTime localDateTime=LocalDateTime.now();
}
