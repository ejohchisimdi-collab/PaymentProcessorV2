package com.chisimidi.ledger.service.repositories;

import com.chisimidi.ledger.service.models.LedgerEntries;
import com.chisimidi.ledger.service.models.Split;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SplitRepository extends JpaRepository<Split,Integer> {
    Split findByLedgerPaymentIdAndLedgerMerchantId(int paymentId, int merchantId);
    List<Split>findByDoneAndMaturingDateBefore(Boolean done, Instant instant);
}
