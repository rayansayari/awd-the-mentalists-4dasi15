package com.reserv.catalogue.dto;

import com.reserv.catalogue.entity.ServiceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de requete pour activer ou desactiver une prestation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceStatusUpdateRequest {

    @NotNull(message = "Le statut est obligatoire (Available ou Inavailable)")
    private ServiceStatus status;
}
