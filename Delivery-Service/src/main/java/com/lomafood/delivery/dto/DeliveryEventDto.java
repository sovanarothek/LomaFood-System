package com.lomafood.delivery.dto;

import com.lomafood.delivery.entity.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryEventDto {
    private Long deliveryId;
    private Long orderId;
    private Long riderId;
    private DeliveryStatus status;
    private String message;
}
