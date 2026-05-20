package com.lomafood.restaurant.controller;

import com.lomafood.restaurant.dto.*;
import com.lomafood.restaurant.service.RestaurantService;
import com.lomafood.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    // ---- PUBLIC ----
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getOpenRestaurants(
            @RequestParam(required = false) String city) {
        return ResponseEntity.ok(restaurantService.getOpenRestaurants(city));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getRestaurantById(@PathVariable UUID id) {
        return ResponseEntity.ok(restaurantService.getRestaurantById(id));
    }

    @GetMapping("/{id}/menu")
    public ResponseEntity<ApiResponse<?>> getMenu(@PathVariable UUID id) {
        return ResponseEntity.ok(restaurantService.getMenu(id));
    }

    @GetMapping("/{id}/business-hours")
    public ResponseEntity<ApiResponse<?>> getBusinessHours(@PathVariable UUID id) {
        return ResponseEntity.ok(restaurantService.getBusinessHours(id));
    }

    // ---- RESTAURANT OWNER ----
    @PostMapping
    public ResponseEntity<ApiResponse<?>> registerRestaurant(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody RestaurantRequest req) {
        return ResponseEntity.ok(restaurantService.registerRestaurant(email, email, req));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyRestaurants(
            @RequestHeader("X-User-Email") String email) {
        return ResponseEntity.ok(restaurantService.getMyRestaurants(email));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateRestaurant(
            @RequestHeader("X-User-Email") String email,
            @PathVariable UUID id,
            @Valid @RequestBody RestaurantRequest req) {
        return ResponseEntity.ok(restaurantService.updateRestaurant(email, id, req));
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<?>> toggleStatus(
            @RequestHeader("X-User-Email") String email,
            @PathVariable UUID id) {
        return ResponseEntity.ok(restaurantService.toggleOpenStatus(email, id));
    }

    @PostMapping("/{id}/business-hours")
    public ResponseEntity<ApiResponse<?>> setBusinessHours(
            @RequestHeader("X-User-Email") String email,
            @PathVariable UUID id,
            @Valid @RequestBody BusinessHoursRequest req) {
        return ResponseEntity.ok(restaurantService.setBusinessHours(email, id, req));
    }

    // ---- MENU MANAGEMENT ----
    @PostMapping("/{id}/menu")
    public ResponseEntity<ApiResponse<?>> addMenuItem(
            @RequestHeader("X-User-Email") String email,
            @PathVariable UUID id,
            @Valid @RequestBody MenuItemRequest req) {
        return ResponseEntity.ok(restaurantService.addMenuItem(email, id, req));
    }

    @PutMapping("/{id}/menu/{itemId}")
    public ResponseEntity<ApiResponse<?>> updateMenuItem(
            @RequestHeader("X-User-Email") String email,
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @Valid @RequestBody MenuItemRequest req) {
        return ResponseEntity.ok(restaurantService.updateMenuItem(email, id, itemId, req));
    }

    @DeleteMapping("/{id}/menu/{itemId}")
    public ResponseEntity<ApiResponse<?>> deleteMenuItem(
            @RequestHeader("X-User-Email") String email,
            @PathVariable UUID id,
            @PathVariable UUID itemId) {
        return ResponseEntity.ok(restaurantService.deleteMenuItem(email, id, itemId));
    }

    // ---- ADMIN ----
    @GetMapping("/admin/pending")
    public ResponseEntity<ApiResponse<?>> getPendingRestaurants() {
        return ResponseEntity.ok(restaurantService.getPendingRestaurants());
    }

    @PatchMapping("/admin/{id}/approve")
    public ResponseEntity<ApiResponse<?>> approveRestaurant(@PathVariable UUID id) {
        return ResponseEntity.ok(restaurantService.approveRestaurant(id));
    }

    @PatchMapping("/admin/{id}/reject")
    public ResponseEntity<ApiResponse<?>> rejectRestaurant(@PathVariable UUID id) {
        return ResponseEntity.ok(restaurantService.rejectRestaurant(id));
    }
}
