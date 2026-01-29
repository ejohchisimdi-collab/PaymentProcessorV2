package com.chisimidi.payment.service.repositories;

import com.chisimidi.payment.service.models.Payment;
import com.chisimidi.payment.service.models.PaymentIdempotency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentIdempotencyRepository extends JpaRepository<PaymentIdempotency,Integer> {
    PaymentIdempotency findByIdempotencyKey(String idempotencyKey);
}
