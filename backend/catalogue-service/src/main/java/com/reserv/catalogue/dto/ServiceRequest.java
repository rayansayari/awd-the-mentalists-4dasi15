package com.reserv.catalogue.dto;

import com.reserv.catalogue.entity.ServiceStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO de requete pour la creation et la mise a jour d'une prestation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequest {

    @NotBlank(message = "Le nom de la prestation est obligatoire")
    @Size(max = 150, message = "Le nom de la prestation ne doit pas depasser 150 caracteres")
    private String name;

    @Size(max = 1000, message = "La description de la prestation ne doit pas depasser 1000 caracteres")
    private String description;

    @NotNull(message = "Le prix de la prestation est obligatoire")
    @DecimalMin(value = "0.0", inclusive = true, message = "Le prix doit etre positif ou nul")
    private BigDecimal price;

    @NotNull(message = "La duree de la prestation est obligatoire")
    @Positive(message = "La duree doit etre un entier strictement positif")
    private Integer duration;

    @NotNull(message = "L'identifiant de la categorie est obligatoire")
    @Positive(message = "L'identifiant de la categorie doit etre positif")
    private Long categoryId;

    private ServiceStatus status;
}
