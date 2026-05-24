package com.lomafood.delivery.controller;

import com.lomafood.delivery.dto.*;
import com.lomafood.delivery.entity.Delivery;
import com.lomafood.delivery.entity.DeliveryStatus;
import com.lomafood.delivery.service.DeliveryService;
import com.lomafood.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping
    public ResponseEntity<ApiResponse<Delivery>> createDelivery(
            @RequestBody CreateDeliveryRequest request) {
        Delivery delivery = deliveryService.createDelivery(request);
        return ResponseEntity.ok(ApiResponse.success("Delivery created", delivery));
    }

    @PutMapping("/{deliveryId}/assign-rider/{riderId}")
    public ResponseEntity<ApiResponse<Delivery>> assignRider(
            @PathVariable Long deliveryId,
            @PathVariable Long riderId) {
        Delivery delivery = deliveryService.assignRider(deliveryId, riderId);
        return ResponseEntity.ok(ApiResponse.success("Rider assigned", delivery));
    }

    @PutMapping("/{deliveryId}/status")
    public ResponseEntity<ApiResponse<Delivery>> updateStatus(
            @PathVariable Long deliveryId,
            @RequestBody UpdateDeliveryStatusRequest request) {
        Delivery delivery = deliveryService.updateStatus(deliveryId, request);
        return ResponseEntity.ok(ApiResponse.success("Status updated", delivery));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<Delivery>> getByOrderId(
            @PathVariable Long orderId) {
        Delivery delivery = deliveryService.getDeliveryByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success("Delivery fetched", delivery));
    }

    @GetMapping("/rider/{riderId}")
    public ResponseEntity<ApiResponse<List<Delivery>>> getRiderDeliveries(
            @PathVariable Long riderId) {
        List<Delivery> deliveries = deliveryService.getRiderDeliveries(riderId);
        return ResponseEntity.ok(ApiResponse.success("Rider deliveries fetched", deliveries));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<Delivery>>> getByStatus(
            @PathVariable DeliveryStatus status) {
        List<Delivery> deliveries = deliveryService.getDeliveriesByStatus(status);
        return ResponseEntity.ok(ApiResponse.success("Deliveries fetched", deliveries));
    }
}
