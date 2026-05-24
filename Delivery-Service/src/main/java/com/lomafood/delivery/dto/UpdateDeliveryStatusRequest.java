package com.lomafood.delivery.dto;

import com.lomafood.delivery.entity.DeliveryStatus;
import lombok.Data;

@Data
public class UpdateDeliveryStatusRequest {
    private DeliveryStatus status;
    private String failureReason;
}
