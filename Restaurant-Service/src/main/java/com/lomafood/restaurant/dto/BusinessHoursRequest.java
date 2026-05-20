package com.lomafood.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class BusinessHoursRequest {
    private List<HoursEntry> hours;

    @Data
    public static class HoursEntry {
        @NotBlank
        private String dayOfWeek;
        private String openTime;
        private String closeTime;
        private boolean closed;
    }
}
