package com.lomafood.user.dto;

import com.lomafood.user.entity.UserRole;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserProfileResponse {
    private UUID id;
    private String email;
    private String name;
    private String phone;
    private String avatarUrl;
    private String bio;
    private UserRole role;
    private boolean active;
    private LocalDateTime createdAt;
}
