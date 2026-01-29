package com.chisimdi.webhook.service.repositories;

import com.chisimdi.webhook.service.models.EventsIdempotency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventsIdempotencyRepository extends JpaRepository<EventsIdempotency,String> {
}
