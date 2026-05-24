package com.lomafood.delivery.dto;

import lombok.Data;

@Data
public class CreateDeliveryRequest {
    private Long orderId;
    private Long restaurantId;
    private String pickupAddress;
    private String deliveryAddress;
    private Double pickupLat;
    private Double pickupLng;
    private Double deliveryLat;
    private Double deliveryLng;
}
