package com.chisimidi.ledger.service.repositories;

import com.chisimidi.ledger.service.models.EventIdempotency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventsIdempotencyRepository extends JpaRepository<EventIdempotency,String> {
}
