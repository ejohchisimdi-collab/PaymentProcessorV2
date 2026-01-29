package com.chisimidi.payment.service.repositories;

import com.chisimidi.payment.service.models.Refunds;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefundsRepository extends JpaRepository<Refunds,Integer> {
    Page<Refunds>findByMerchantId(int merchantId, Pageable pageable);
}
