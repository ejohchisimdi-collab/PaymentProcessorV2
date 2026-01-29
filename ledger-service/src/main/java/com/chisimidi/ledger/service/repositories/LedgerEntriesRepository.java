package com.chisimidi.ledger.service.repositories;

import com.chisimidi.ledger.service.models.Ledger;
import com.chisimidi.ledger.service.models.LedgerEntries;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LedgerEntriesRepository extends JpaRepository<LedgerEntries,Integer> {
    Page<LedgerEntries>findByLedgerPaymentIdAndLedgerMerchantId(int paymentId, int merchantId, Pageable pageable);

}
