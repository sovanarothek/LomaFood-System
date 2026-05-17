package com.lomafood.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleAuthRequest {
    @NotBlank @Email
    private String email;

    @NotBlank
    private String name;

    @NotBlank
    private String providerId;
}
