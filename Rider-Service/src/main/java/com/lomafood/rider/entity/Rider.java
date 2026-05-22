package com.lomafood.rider.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "riders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rider {

    @Id
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    private String name;
    private String phone;
    private String avatarUrl;

    // Vehicle info
    private String vehicleType; // MOTORBIKE, BICYCLE, CAR
    private String vehiclePlate;

    // Location
    private Double latitude;
    private Double longitude;
    private LocalDateTime locationUpdatedAt;

    @Enumerated(EnumType.STRING)
    private RiderStatus status;

    @Enumerated(EnumType.STRING)
    private RiderAvailability availability;

    // Stats
    private Integer totalDeliveries;
    private Double totalEarnings;
    private Double rating;
    private Integer totalRatings;

    private boolean verified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = RiderStatus.PENDING;
        this.availability = RiderAvailability.OFFLINE;
        this.totalDeliveries = 0;
        this.totalEarnings = 0.0;
        this.rating = 0.0;
        this.totalRatings = 0;
        this.verified = false;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
