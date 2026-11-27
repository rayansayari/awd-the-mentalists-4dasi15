package com.awd.provider.dto;

import jakarta.validation.constraints.*;

/** Corps des requetes POST/PUT /api/providers. Le statut n'est pas modifiable ici (PATCH activate/suspend). */
public record ProviderRequest(
        @NotNull(message = "userId est obligatoire") Long userId,
        @NotBlank(message = "firstName est obligatoire") @Size(max = 100) String firstName,
        @NotBlank(message = "lastName est obligatoire") @Size(max = 100) String lastName,
        @NotBlank(message = "email est obligatoire") @Email(message = "email invalide") @Size(max = 150) String email,
        @Size(max = 30) String phone,
        @Size(max = 1000) String description,
        @Min(value = 0, message = "experienceYears doit etre >= 0") Integer experienceYears
) {}
