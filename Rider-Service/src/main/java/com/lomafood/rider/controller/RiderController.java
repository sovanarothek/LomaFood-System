package com.lomafood.rider.controller;

import com.lomafood.rider.dto.*;
import com.lomafood.rider.service.RiderService;
import com.lomafood.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/riders")
@RequiredArgsConstructor
public class RiderController {

    private final RiderService riderService;

    // ---- RIDER ----
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> registerRider(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody RegisterRiderRequest req) {
        return ResponseEntity.ok(riderService.registerRider(
                email, UUID.fromString(userId), req));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<?>> getProfile(
            @RequestHeader("X-User-Email") String email) {
        return ResponseEntity.ok(riderService.getProfile(email));
    }

    @PatchMapping("/availability")
    public ResponseEntity<ApiResponse<?>> toggleAvailability(
            @RequestHeader("X-User-Email") String email) {
        return ResponseEntity.ok(riderService.toggleAvailability(email));
    }

    @PutMapping("/location")
    public ResponseEntity<ApiResponse<?>> updateLocation(
            @RequestHeader("X-User-Email") String email,
            @Valid @RequestBody UpdateLocationRequest req) {
        return ResponseEntity.ok(riderService.updateLocation(email, req));
    }

    @PostMapping("/orders/{orderId}/accept")
    public ResponseEntity<ApiResponse<?>> acceptOrder(
            @RequestHeader("X-User-Email") String email,
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(riderService.acceptOrder(email, orderId));
    }

    @PostMapping("/orders/{orderId}/complete")
    public ResponseEntity<ApiResponse<?>> completeDelivery(
            @RequestHeader("X-User-Email") String email,
            @PathVariable UUID orderId,
            @RequestParam Double earnings) {
        return ResponseEntity.ok(riderService.completeDelivery(email, orderId, earnings));
    }

    // ---- EARNINGS ----
    @GetMapping("/earnings")
    public ResponseEntity<ApiResponse<?>> getEarnings(
            @RequestHeader("X-User-Email") String email) {
        return ResponseEntity.ok(riderService.getEarnings(email));
    }

    @GetMapping("/earnings/unpaid")
    public ResponseEntity<ApiResponse<?>> getUnpaidEarnings(
            @RequestHeader("X-User-Email") String email) {
        return ResponseEntity.ok(riderService.getUnpaidEarnings(email));
    }

    // ---- RATINGS ----
    @PostMapping("/{riderId}/rate")
    public ResponseEntity<ApiResponse<?>> rateRider(
            @RequestHeader("X-User-Email") String email,
            @PathVariable UUID riderId,
            @Valid @RequestBody RateRiderRequest req) {
        return ResponseEntity.ok(riderService.rateRider(email, riderId, req));
    }

    @GetMapping("/{riderId}/ratings")
    public ResponseEntity<ApiResponse<?>> getRatings(
            @PathVariable UUID riderId) {
        return ResponseEntity.ok(riderService.getRatings(riderId));
    }

    // ---- PUBLIC ----
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<?>> getAvailableRiders() {
        return ResponseEntity.ok(riderService.getAvailableRiders());
    }

    // ---- ADMIN ----
    @GetMapping("/admin/pending")
    public ResponseEntity<ApiResponse<?>> getPendingRiders() {
        return ResponseEntity.ok(riderService.getPendingRiders());
    }

    @PatchMapping("/admin/{riderId}/approve")
    public ResponseEntity<ApiResponse<?>> approveRider(@PathVariable UUID riderId) {
        return ResponseEntity.ok(riderService.approveRider(riderId));
    }

    @PatchMapping("/admin/{riderId}/reject")
    public ResponseEntity<ApiResponse<?>> rejectRider(@PathVariable UUID riderId) {
        return ResponseEntity.ok(riderService.rejectRider(riderId));
    }
}
