package com.lomafood.payment.repository;

import com.lomafood.payment.entity.Payment;
import com.lomafood.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByOrderId(UUID orderId);
    List<Payment> findByUserId(UUID userId);
    List<Payment> findByStatus(PaymentStatus status);
    boolean existsByOrderId(UUID orderId);
}
