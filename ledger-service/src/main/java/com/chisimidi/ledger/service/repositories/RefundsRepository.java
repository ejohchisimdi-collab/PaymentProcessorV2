package com.chisimidi.ledger.service.repositories;

import com.chisimidi.ledger.service.models.LedgerEntries;
import com.chisimidi.ledger.service.models.Refunds;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefundsRepository extends JpaRepository<Refunds,Integer> {
    Refunds findByLedgerPaymentIdAndLedgerMerchantId(int paymentId, int merchantId);
}
