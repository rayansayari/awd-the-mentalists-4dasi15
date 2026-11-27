package com.awd.provider.mapper;

import com.awd.provider.dto.ProviderRequest;
import com.awd.provider.dto.ProviderResponse;
import com.awd.provider.entity.Provider;
import org.springframework.stereotype.Component;

/** Convertit Entity <-> DTO (evite d'exposer les entites et les boucles JSON). */
@Component
public class ProviderMapper {

    public Provider toEntity(ProviderRequest r) {
        Provider p = new Provider();
        apply(p, r);
        return p;
    }

    /** Copie les champs du DTO dans une entite existante (utilise pour PUT). */
    public void apply(Provider p, ProviderRequest r) {
        p.setUserId(r.userId());
        p.setFirstName(r.firstName());
        p.setLastName(r.lastName());
        p.setEmail(r.email());
        p.setPhone(r.phone());
        p.setDescription(r.description());
        p.setExperienceYears(r.experienceYears());
    }

    public ProviderResponse toResponse(Provider p) {
        return new ProviderResponse(p.getId(), p.getUserId(), p.getFirstName(), p.getLastName(),
                p.getEmail(), p.getPhone(), p.getDescription(), p.getExperienceYears(),
                p.getStatus(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
