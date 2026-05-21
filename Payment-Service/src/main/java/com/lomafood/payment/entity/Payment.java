package com.lomafood.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private UUID orderId;

    private UUID userId;
    private String userEmail;

    @Enumerated(EnumType.STRING)
    private PaymentProvider provider;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private Double amount;
    private String currency;

    // QR Code
    private String qrCodeData;
    private String qrCodeImageUrl;

    // Transaction
    private String transactionId;
    private String providerReference;

    // Refund
    private Double refundAmount;
    private String refundReason;
    private LocalDateTime refundedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = PaymentStatus.PENDING;
        this.currency = "USD";
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
