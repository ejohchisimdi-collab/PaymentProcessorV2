package com.chisimidi.ledger.service.repositories;

import com.chisimidi.ledger.service.models.Ledger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LedgerRepository extends JpaRepository<Ledger,Integer> {
Ledger findByPaymentId(int paymentId);
Ledger findByMerchantIdAndPaymentId(int merchantId,int paymentId);
}
