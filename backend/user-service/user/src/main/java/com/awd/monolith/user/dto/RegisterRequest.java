package com.awd.monolith.user.dto;

import com.awd.monolith.user.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @NotBlank(message = "Lastname is required")
        @Size(max = 100, message = "Lastname must not exceed 100 characters")
        String lastname,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String password,

        Role role,

        @Size(max = 500, message = "Image URL must not exceed 500 characters")
        String imageUrl
) {
}
