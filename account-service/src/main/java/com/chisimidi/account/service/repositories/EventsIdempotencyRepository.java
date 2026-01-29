package com.chisimidi.account.service.repositories;

import com.chisimidi.account.service.models.EventsIdempotency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventsIdempotencyRepository extends JpaRepository<EventsIdempotency,String> {
}
