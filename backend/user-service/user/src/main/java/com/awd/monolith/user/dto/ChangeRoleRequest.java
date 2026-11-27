package com.awd.monolith.user.dto;

import com.awd.monolith.user.entity.Role;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(
        @NotNull(message = "Role is required")
        Role role
) {
}
