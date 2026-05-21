package com.lomafood.order.dto;

import com.lomafood.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEventDto {
    private UUID orderId;
    private UUID userId;
    private String userEmail;
    private UUID restaurantId;
    private String restaurantName;
    private OrderStatus status;
    private Double total;
    private String paymentMethod;
}
