package com.lomafood.order.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class CreateOrderRequest {

    @NotNull
    private UUID restaurantId;
    private String restaurantName;

    @NotEmpty
    private List<OrderItemRequest> items;

    private Double deliveryFee;

    // Delivery address
    @NotNull
    private String deliveryStreet;
    private String deliveryCity;
    private Double deliveryLatitude;
    private Double deliveryLongitude;

    private String paymentMethod;
    private String notes;

    @Data
    public static class OrderItemRequest {
        private UUID menuItemId;
        private String name;
        private Double price;
        private Integer quantity;
        private String notes;
    }
}
