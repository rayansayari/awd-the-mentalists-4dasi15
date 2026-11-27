package com.awd.provider.service;

import com.awd.provider.dto.ProviderRequest;
import com.awd.provider.dto.ProviderResponse;
import com.awd.provider.entity.Provider;
import com.awd.provider.enums.ProviderStatus;
import com.awd.provider.exception.ConflictException;
import com.awd.provider.exception.ResourceNotFoundException;
import com.awd.provider.mapper.ProviderMapper;
import com.awd.provider.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Logique metier des prestataires (createProfile, updateProfile, getProfile, activate, suspend). */
@Service
@RequiredArgsConstructor
@Transactional
public class ProviderService {

    private final ProviderRepository providerRepository;
    private final ProviderMapper mapper;

    /** createProfile() : un nouveau prestataire commence en PENDING. */
    public ProviderResponse createProfile(ProviderRequest request) {
        if (providerRepository.existsByEmail(request.email())) {
            throw new ConflictException("Un prestataire avec l'email " + request.email() + " existe deja");
        }
        Provider provider = mapper.toEntity(request);
        provider.setStatus(ProviderStatus.PENDING);
        return mapper.toResponse(providerRepository.save(provider));
    }

    @Transactional(readOnly = true)
    public List<ProviderResponse> getAll() {
        return providerRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    /** getProfile() */
    @Transactional(readOnly = true)
    public ProviderResponse getProfile(Long id) {
        return mapper.toResponse(findOrThrow(id));
    }

    /** updateProfile() */
    public ProviderResponse updateProfile(Long id, ProviderRequest request) {
        Provider provider = findOrThrow(id);
        if (providerRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new ConflictException("L'email " + request.email() + " est deja utilise par un autre prestataire");
        }
        mapper.apply(provider, request);
        return mapper.toResponse(providerRepository.save(provider));
    }

    /** Supprime le prestataire et (cascade) ses skills, zones et disponibilites. */
    public void delete(Long id) {
        providerRepository.delete(findOrThrow(id));
    }

    public ProviderResponse activate(Long id) {
        Provider provider = findOrThrow(id);
        if (provider.getStatus() == ProviderStatus.ACTIVE) {
            throw new ConflictException("Le prestataire " + id + " est deja ACTIVE");
        }
        provider.activate();
        return mapper.toResponse(providerRepository.save(provider));
    }

    public ProviderResponse suspend(Long id) {
        Provider provider = findOrThrow(id);
        if (provider.getStatus() == ProviderStatus.SUSPENDED) {
            throw new ConflictException("Le prestataire " + id + " est deja SUSPENDED");
        }
        provider.suspend();
        return mapper.toResponse(providerRepository.save(provider));
    }

    /** Utilise aussi par les autres services pour verifier l'existence d'un Provider. */
    public Provider findOrThrow(Long id) {
        return providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prestataire introuvable avec l'id " + id));
    }
}
