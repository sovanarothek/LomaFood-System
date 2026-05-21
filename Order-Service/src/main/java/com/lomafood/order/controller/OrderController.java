package com.lomafood.order.controller;

import com.lomafood.order.dto.*;
import com.lomafood.order.entity.OrderStatus;
import com.lomafood.order.service.OrderService;
import com.lomafood.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ---- USER ----
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createOrder(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateOrderRequest req) {
        return ResponseEntity.ok(orderService.createOrder(email, UUID.fromString(userId), req));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyOrders(
            @RequestHeader("X-User-Email") String email) {
        return ResponseEntity.ok(orderService.getMyOrders(email));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<?>> getOrderById(
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<?>> cancelOrder(
            @RequestHeader("X-User-Email") String email,
            @PathVariable UUID orderId,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(orderService.cancelOrder(email, orderId, reason));
    }

    @PostMapping("/{orderId}/reorder")
    public ResponseEntity<ApiResponse<?>> reorder(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.reorder(email, UUID.fromString(userId), orderId));
    }

    // ---- RESTAURANT ----
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse<?>> getRestaurantOrders(
            @PathVariable UUID restaurantId,
            @RequestParam(required = false) OrderStatus status) {
        return ResponseEntity.ok(orderService.getRestaurantOrders(restaurantId, status));
    }

    // ---- RIDER ----
    @GetMapping("/rider/{riderId}")
    public ResponseEntity<ApiResponse<?>> getRiderOrders(
            @PathVariable UUID riderId) {
        return ResponseEntity.ok(orderService.getRiderOrders(riderId));
    }

    // ---- RESTAURANT/RIDER STATUS UPDATE ----
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<?>> updateStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest req) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, req));
    }
}
