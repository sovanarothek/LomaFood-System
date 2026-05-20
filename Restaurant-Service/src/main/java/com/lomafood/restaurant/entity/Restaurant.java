package com.lomafood.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "restaurants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;
    private String imageUrl;
    private String phone;
    private String email;

    // Location
    private String street;
    private String city;
    private String country;
    private Double latitude;
    private Double longitude;

    // Owner
    private UUID ownerId;
    private String ownerEmail;

    @Enumerated(EnumType.STRING)
    private RestaurantStatus status;

    private boolean open;
    private Double rating;
    private Integer totalRatings;
    private Integer deliveryTimeMinutes;
    private Double deliveryFee;
    private Double minimumOrder;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = RestaurantStatus.PENDING;
        this.rating = 0.0;
        this.totalRatings = 0;
        this.open = false;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
