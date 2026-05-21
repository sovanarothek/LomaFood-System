package com.lomafood.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class RefundRequest {
    @NotNull
    private UUID paymentId;

    @NotNull
    private Double amount;

    @NotBlank
    private String reason;
}
