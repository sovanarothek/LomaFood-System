package com.lomafood.payment.dto;

import lombok.Data;

@Data
public class PaymentCallbackRequest {
    private String transactionId;
    private String providerReference;
    private String status;
    private Double amount;
    private String currency;
    private String signature;
}
