package com.awd.monolith.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
        String name,

        @Size(min = 1, max = 100, message = "Lastname must be between 1 and 100 characters")
        String lastname,

        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @Size(max = 500, message = "Image URL must not exceed 500 characters")
        String imageUrl
) {
}
