package com.lomafood.user.dto;

import com.lomafood.user.entity.PaymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentMethodRequest {
    @NotNull
    private PaymentType type; // ABA, WING, ACLEDA, PPCBANK, BAKONG_QR, CARD, CASH

    @NotBlank
    private String provider;

    private String maskedNumber;  // for cards
    private String phoneNumber;   // for WING, ABA mobile
    private String accountNumber; // for ACLEDA, PPCBANK
    private String qrCode;        // for Bakong QR
    private String expiryDate;    // for cards
    private boolean isDefault;
}
