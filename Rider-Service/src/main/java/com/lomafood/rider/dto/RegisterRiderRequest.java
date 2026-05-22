package com.lomafood.rider.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRiderRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String phone;
    @NotBlank
    private String vehicleType;
    @NotBlank
    private String vehiclePlate;
}
