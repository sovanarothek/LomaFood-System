package com.lomafood.rider.dto;

import com.lomafood.rider.entity.RiderAvailability;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RiderEventDto {
    private UUID riderId;
    private String riderEmail;
    private UUID orderId;
    private RiderAvailability availability;
    private Double latitude;
    private Double longitude;
}
