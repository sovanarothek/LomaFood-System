package com.lomafood.order.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID userId;
    private String userEmail;

    private UUID restaurantId;
    private String restaurantName;

    private UUID riderId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> items;

    private Double subtotal;
    private Double deliveryFee;
    private Double total;

    // Delivery address
    private String deliveryStreet;
    private String deliveryCity;
    private Double deliveryLatitude;
    private Double deliveryLongitude;

    // Payment
    private String paymentMethod;
    private boolean paid;

    private String cancelReason;
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = OrderStatus.PENDING;
        this.paid = false;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
