package com.lomafood.payment.dto;

import com.lomafood.payment.entity.PaymentProvider;
import com.lomafood.payment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEventDto {
    private UUID paymentId;
    private UUID orderId;
    private UUID userId;
    private String userEmail;
    private PaymentProvider provider;
    private PaymentStatus status;
    private Double amount;
    private String transactionId;
}
