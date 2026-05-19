package com.lomafood.user.controller;

import com.lomafood.user.dto.*;
import com.lomafood.user.service.UserService;
import com.lomafood.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ---- PROFILE ----
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<?>> getProfile(
            @RequestHeader("X-User-Email") String email) {
        return ResponseEntity.ok(userService.getProfile(email));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<?>> updateProfile(
            @RequestHeader("X-User-Email") String email,
            @Valid @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(userService.updateProfile(email, req));
    }

    @PutMapping("/profile/avatar")
    public ResponseEntity<ApiResponse<?>> updateAvatar(
            @RequestHeader("X-User-Email") String email,
            @RequestParam String avatarUrl) {
        return ResponseEntity.ok(userService.updateAvatar(email, avatarUrl));
    }

    @DeleteMapping("/profile")
    public ResponseEntity<ApiResponse<?>> deleteAccount(
            @RequestHeader("X-User-Email") String email) {
        return ResponseEntity.ok(userService.deleteAccount(email));
    }

    // ---- ADDRESSES ----
    @GetMapping("/addresses")
    public ResponseEntity<ApiResponse<?>> getAddresses(
            @RequestHeader("X-User-Email") String email) {
        return ResponseEntity.ok(userService.getAddresses(email));
    }

    @PostMapping("/addresses")
    public ResponseEntity<ApiResponse<?>> addAddress(
            @RequestHeader("X-User-Email") String email,
            @Valid @RequestBody AddressRequest req) {
        return ResponseEntity.ok(userService.addAddress(email, req));
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<ApiResponse<?>> updateAddress(
            @RequestHeader("X-User-Email") String email,
            @PathVariable UUID addressId,
            @Valid @RequestBody AddressRequest req) {
        return ResponseEntity.ok(userService.updateAddress(email, addressId, req));
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<ApiResponse<?>> deleteAddress(
            @PathVariable UUID addressId) {
        return ResponseEntity.ok(userService.deleteAddress(addressId));
    }

    // ---- FAVORITES ----
    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<?>> getFavorites(
            @RequestHeader("X-User-Email") String email) {
        return ResponseEntity.ok(userService.getFavorites(email));
    }

    @PostMapping("/favorites")
    public ResponseEntity<ApiResponse<?>> addFavorite(
            @RequestHeader("X-User-Email") String email,
            @Valid @RequestBody FavoriteRequest req) {
        return ResponseEntity.ok(userService.addFavorite(email, req));
    }

    @DeleteMapping("/favorites/{restaurantId}")
    public ResponseEntity<ApiResponse<?>> removeFavorite(
            @RequestHeader("X-User-Email") String email,
            @PathVariable UUID restaurantId) {
        return ResponseEntity.ok(userService.removeFavorite(email, restaurantId));
    }

    // ---- PAYMENT METHODS ----
    @GetMapping("/payment-methods")
    public ResponseEntity<ApiResponse<?>> getPaymentMethods(
            @RequestHeader("X-User-Email") String email) {
        return ResponseEntity.ok(userService.getPaymentMethods(email));
    }

    @PostMapping("/payment-methods")
    public ResponseEntity<ApiResponse<?>> addPaymentMethod(
            @RequestHeader("X-User-Email") String email,
            @Valid @RequestBody PaymentMethodRequest req) {
        return ResponseEntity.ok(userService.addPaymentMethod(email, req));
    }

    @DeleteMapping("/payment-methods/{paymentId}")
    public ResponseEntity<ApiResponse<?>> deletePaymentMethod(
            @PathVariable UUID paymentId) {
        return ResponseEntity.ok(userService.deletePaymentMethod(paymentId));
    }
}
