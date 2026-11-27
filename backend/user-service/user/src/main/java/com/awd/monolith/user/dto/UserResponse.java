package com.awd.monolith.user.dto;

import com.awd.monolith.user.entity.Role;
import com.awd.monolith.user.entity.User;
import java.time.Instant;

public record UserResponse(
        Long id,
        String name,
        String lastname,
        String email,
        Role role,
        String imageUrl,
        Instant createdAt,
        Instant updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getLastname(),
                user.getEmail(),
                user.getRole(),
                user.getImageUrl(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
