package com.reserv.catalogue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de requete pour la creation et la mise a jour d'une categorie.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {

    @NotBlank(message = "Le nom de la categorie est obligatoire")
    @Size(max = 100, message = "Le nom de la categorie ne doit pas depasser 100 caracteres")
    private String name;

    @Size(max = 500, message = "La description de la categorie ne doit pas depasser 500 caracteres")
    private String description;
}
