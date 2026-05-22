package com.lomafood.rider.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rider_earnings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Earning {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID riderId;
    private UUID orderId;
    private Double amount;
    private String description;
    private boolean paid;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.paid = false;
    }
}
