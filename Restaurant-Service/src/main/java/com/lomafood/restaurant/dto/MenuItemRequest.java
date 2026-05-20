package com.lomafood.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MenuItemRequest {
    @NotBlank
    private String name;
    private String description;
    private String imageUrl;

    @NotNull
    private Double price;
    private String category;
    private boolean available;
    private boolean popular;
}
