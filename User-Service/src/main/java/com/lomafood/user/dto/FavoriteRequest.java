package com.lomafood.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class FavoriteRequest {
    @NotNull
    private UUID restaurantId;
    private String restaurantName;
    private String restaurantImage;
}
