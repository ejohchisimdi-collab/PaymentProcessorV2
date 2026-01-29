package com.chisimidi.payment.service.repositories;

import com.chisimidi.payment.service.models.RefundIdempotency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefundIdempotencyRepository extends JpaRepository<RefundIdempotency,Integer> {
    RefundIdempotency findByIdempotencyKey(String idempotencyKey);
}
