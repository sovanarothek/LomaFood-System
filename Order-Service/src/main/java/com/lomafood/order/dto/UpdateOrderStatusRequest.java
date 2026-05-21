package com.lomafood.order.dto;

import com.lomafood.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    @NotNull
    private OrderStatus status;
    private String cancelReason;
}
