package com.awd.provider.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SkillRequest(
        @NotBlank(message = "name est obligatoire") @Size(max = 100) String name,
        @Size(max = 500) String description
) {}
