package com.lomafood.rider.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rider_ratings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiderRating {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID riderId;
    private UUID orderId;
    private UUID userId;
    private String userEmail;

    private Integer rating; // 1-5
    private String comment;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
