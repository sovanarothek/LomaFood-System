package com.lomafood.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "business_hours")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessHours {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    private String dayOfWeek; // MONDAY, TUESDAY...
    private String openTime;  // 08:00
    private String closeTime; // 22:00
    private boolean closed;
}
