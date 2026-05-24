package com.lomafood.delivery.repository;

import com.lomafood.delivery.entity.Delivery;
import com.lomafood.delivery.entity.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByOrderId(Long orderId);
    List<Delivery> findByRiderId(Long riderId);
    List<Delivery> findByStatus(DeliveryStatus status);
    List<Delivery> findByRiderIdAndStatus(Long riderId, DeliveryStatus status);
}
