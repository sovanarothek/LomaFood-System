package com.lomafood.order.repository;

import com.lomafood.order.entity.Order;
import com.lomafood.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByUserEmailOrderByCreatedAtDesc(String userEmail);
    List<Order> findByRestaurantIdOrderByCreatedAtDesc(UUID restaurantId);
    List<Order> findByRiderIdOrderByCreatedAtDesc(UUID riderId);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByRestaurantIdAndStatus(UUID restaurantId, OrderStatus status);
}
