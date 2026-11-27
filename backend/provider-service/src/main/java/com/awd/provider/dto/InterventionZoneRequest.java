package com.awd.provider.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InterventionZoneRequest(
        @NotBlank(message = "city est obligatoire") @Size(max = 100) String city,
        @Size(max = 100) String region,
        @Size(max = 10) String postalCode
) {}
