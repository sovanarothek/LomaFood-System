package com.lomafood.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RestaurantRequest {
    @NotBlank
    private String name;
    private String description;
    private String imageUrl;
    private String phone;
    private String email;
    private String street;
    private String city;
    private String country;
    private Double latitude;
    private Double longitude;
    private Integer deliveryTimeMinutes;
    private Double deliveryFee;
    private Double minimumOrder;
}
