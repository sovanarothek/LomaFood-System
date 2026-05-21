package com.lomafood.payment.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lomafood.payment.dto.*;
import com.lomafood.payment.entity.*;
import com.lomafood.payment.repository.*;
import com.lomafood.shared.dto.ApiResponse;
import com.lomafood.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final TransactionRepository transactionRepo;
    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, PaymentEventDto> kafkaTemplate;

    private static final String PAYMENT_CACHE = "payment:";
    private static final String TOPIC_PAYMENT_SUCCESS = "payment.success";
    private static final String TOPIC_PAYMENT_FAILED = "payment.failed";
    private static final String TOPIC_PAYMENT_REFUNDED = "payment.refunded";

    // ---- INITIATE PAYMENT ----
    @Transactional
    public ApiResponse<?> initiatePayment(String userEmail, UUID userId, InitiatePaymentRequest req) {
        // Check duplicate
        if (paymentRepo.existsByOrderId(req.getOrderId())) {
            throw new AppException(409, "Payment already initiated for this order");
        }

        Payment payment = Payment.builder()
                .orderId(req.getOrderId())
                .userId(userId)
                .userEmail(userEmail)
                .provider(req.getProvider())
                .amount(req.getAmount())
                .currency(req.getCurrency() != null ? req.getCurrency() : "USD")
                .build();

        // Generate QR for QR-based providers
        if (req.getProvider() == PaymentProvider.BAKONG_QR ||
            req.getProvider() == PaymentProvider.ABA ||
            req.getProvider() == PaymentProvider.ACLEDA ||
            req.getProvider() == PaymentProvider.PPCBANK) {
            String qrData = generateQrData(req);
            payment.setQrCodeData(qrData);
            payment.setQrCodeImageUrl(generateQrCodeBase64(qrData));
        }

        paymentRepo.save(payment);

        // Cache payment status
        redisTemplate.opsForValue().set(
            PAYMENT_CACHE + payment.getId(),
            PaymentStatus.PENDING.name(),
            30, TimeUnit.MINUTES
        );

        return ApiResponse.success("Payment initiated", payment);
    }

    // ---- HANDLE CALLBACK ----
    @Transactional
    public ApiResponse<?> handleCallback(UUID paymentId, PaymentCallbackRequest req) {
        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new AppException(404, "Payment not found"));

        PaymentStatus newStatus = "SUCCESS".equalsIgnoreCase(req.getStatus())
                ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;

        payment.setStatus(newStatus);
        payment.setTransactionId(req.getTransactionId());
        payment.setProviderReference(req.getProviderReference());
        paymentRepo.save(payment);

        // Save transaction
        Transaction tx = Transaction.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .provider(payment.getProvider())
                .status(newStatus)
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .transactionId(req.getTransactionId())
                .providerReference(req.getProviderReference())
                .description("Payment via " + payment.getProvider())
                .build();
        transactionRepo.save(tx);

        // Update cache
        redisTemplate.opsForValue().set(
            PAYMENT_CACHE + paymentId,
            newStatus.name(),
            1, TimeUnit.HOURS
        );

        // Publish Kafka event
        PaymentEventDto event = new PaymentEventDto(
                payment.getId(), payment.getOrderId(), payment.getUserId(),
                payment.getUserEmail(), payment.getProvider(),
                newStatus, payment.getAmount(), req.getTransactionId()
        );

        String topic = newStatus == PaymentStatus.SUCCESS
                ? TOPIC_PAYMENT_SUCCESS : TOPIC_PAYMENT_FAILED;
        kafkaTemplate.send(topic, paymentId.toString(), event);

        return ApiResponse.success("Payment " + newStatus.name().toLowerCase(), payment);
    }

    // ---- GET PAYMENT BY ORDER ----
    public ApiResponse<?> getPaymentByOrder(UUID orderId) {
        Payment payment = paymentRepo.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(404, "Payment not found"));
        return ApiResponse.success("Payment fetched", payment);
    }

    // ---- GET MY PAYMENTS ----
    public ApiResponse<?> getMyPayments(UUID userId) {
        return ApiResponse.success("Payments fetched", paymentRepo.findByUserId(userId));
    }

    // ---- GET TRANSACTION HISTORY ----
    public ApiResponse<?> getTransactionHistory(UUID userId) {
        return ApiResponse.success("Transactions fetched", transactionRepo.findByUserId(userId));
    }

    // ---- REFUND ----
    @Transactional
    public ApiResponse<?> refundPayment(RefundRequest req) {
        Payment payment = paymentRepo.findById(req.getPaymentId())
                .orElseThrow(() -> new AppException(404, "Payment not found"));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new AppException(400, "Only successful payments can be refunded");
        }

        if (req.getAmount() > payment.getAmount()) {
            throw new AppException(400, "Refund amount cannot exceed payment amount");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundAmount(req.getAmount());
        payment.setRefundReason(req.getReason());
        payment.setRefundedAt(LocalDateTime.now());
        paymentRepo.save(payment);

        // Save refund transaction
        Transaction tx = Transaction.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .provider(payment.getProvider())
                .status(PaymentStatus.REFUNDED)
                .amount(req.getAmount())
                .currency(payment.getCurrency())
                .description("Refund: " + req.getReason())
                .build();
        transactionRepo.save(tx);

        // Publish Kafka event
        PaymentEventDto event = new PaymentEventDto(
                payment.getId(), payment.getOrderId(), payment.getUserId(),
                payment.getUserEmail(), payment.getProvider(),
                PaymentStatus.REFUNDED, req.getAmount(), null
        );
        kafkaTemplate.send(TOPIC_PAYMENT_REFUNDED, payment.getId().toString(), event);

        return ApiResponse.success("Payment refunded successfully", payment);
    }

    // ---- GENERATE QR DATA ----
    private String generateQrData(InitiatePaymentRequest req) {
        return String.format(
            "LOMAFOOD|%s|%s|%.2f|%s",
            req.getProvider().name(),
            req.getOrderId(),
            req.getAmount(),
            req.getCurrency() != null ? req.getCurrency() : "USD"
        );
    }

    // ---- GENERATE QR CODE BASE64 ----
    private String generateQrCodeBase64(String data) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(data, BarcodeFormat.QR_CODE, 300, 300);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (WriterException | IOException e) {
            log.error("Failed to generate QR code: {}", e.getMessage());
            return null;
        }
    }
}
