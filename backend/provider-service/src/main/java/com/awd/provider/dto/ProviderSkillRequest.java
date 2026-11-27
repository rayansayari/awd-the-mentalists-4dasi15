package com.awd.provider.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** providerId vient de l'URL ; skillId et experienceLevel du body. */
public record ProviderSkillRequest(
        @NotNull(message = "skillId est obligatoire") Long skillId,
        @NotBlank(message = "experienceLevel est obligatoire") @Size(max = 50) String experienceLevel
) {}
