package com.lomafood.payment.repository;

import com.lomafood.payment.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByUserId(UUID userId);
    List<Transaction> findByPaymentId(UUID paymentId);
    List<Transaction> findByOrderId(UUID orderId);
}
