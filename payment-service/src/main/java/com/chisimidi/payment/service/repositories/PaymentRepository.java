package com.chisimidi.payment.service.repositories;

import com.chisimidi.payment.service.models.Payment;
import com.chisimidi.payment.service.models.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment,Integer> {
    Payment findByIdAndPaymentStatusAndMerchantId(int id,PaymentStatus paymentStatus,int merchantId);
    List<Payment>findByCustomerAccount(String customerToken);
    List<Payment> findByAuthorizationDueDateBeforeAndPaymentStatus(LocalDateTime localDateTime, PaymentStatus paymentStatus);
    List<Payment> findByPaymentStatusAndDone(PaymentStatus paymentStatus,Boolean done);
    Page<Payment>findByMerchantId(int merchantId, Pageable pageable);
    Payment findByIdAndPaymentStatus(int id,PaymentStatus paymentStatus);
}
