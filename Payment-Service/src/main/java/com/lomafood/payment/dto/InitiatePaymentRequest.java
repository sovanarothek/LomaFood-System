package com.lomafood.payment.dto;

import com.lomafood.payment.entity.PaymentProvider;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class InitiatePaymentRequest {
    @NotNull
    private UUID orderId;

    @NotNull
    private PaymentProvider provider;

    @NotNull
    private Double amount;

    private String currency;
    private String description;
    private String returnUrl;
    private String cancelUrl;
}
