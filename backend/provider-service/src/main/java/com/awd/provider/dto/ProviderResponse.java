package com.awd.provider.dto;

import com.awd.provider.enums.ProviderStatus;
import java.time.LocalDateTime;

public record ProviderResponse(
        Long id, Long userId, String firstName, String lastName, String email, String phone,
        String description, Integer experienceYears, ProviderStatus status,
        LocalDateTime createdAt, LocalDateTime updatedAt
) {}
