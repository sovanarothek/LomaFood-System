package com.lomafood.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID paymentId;
    private UUID orderId;
    private UUID userId;

    @Enumerated(EnumType.STRING)
    private PaymentProvider provider;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private Double amount;
    private String currency;
    private String transactionId;
    private String providerReference;
    private String description;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
