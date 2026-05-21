package com.lomafood.payment.controller;

import com.lomafood.payment.dto.*;
import com.lomafood.payment.service.PaymentService;
import com.lomafood.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // ---- INITIATE PAYMENT ----
    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<?>> initiatePayment(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody InitiatePaymentRequest req) {
        return ResponseEntity.ok(paymentService.initiatePayment(
                email, UUID.fromString(userId), req));
    }

    // ---- PAYMENT CALLBACK (from payment provider) ----
    @PostMapping("/callback/{paymentId}")
    public ResponseEntity<ApiResponse<?>> handleCallback(
            @PathVariable UUID paymentId,
            @RequestBody PaymentCallbackRequest req) {
        return ResponseEntity.ok(paymentService.handleCallback(paymentId, req));
    }

    // ---- GET PAYMENT BY ORDER ----
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<?>> getPaymentByOrder(
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrder(orderId));
    }

    // ---- GET MY PAYMENTS ----
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyPayments(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(paymentService.getMyPayments(UUID.fromString(userId)));
    }

    // ---- GET TRANSACTION HISTORY ----
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<?>> getTransactionHistory(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(paymentService.getTransactionHistory(
                UUID.fromString(userId)));
    }

    // ---- REFUND ----
    @PostMapping("/refund")
    public ResponseEntity<ApiResponse<?>> refundPayment(
            @Valid @RequestBody RefundRequest req) {
        return ResponseEntity.ok(paymentService.refundPayment(req));
    }
}
