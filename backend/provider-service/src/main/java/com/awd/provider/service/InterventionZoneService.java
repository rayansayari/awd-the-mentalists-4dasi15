package com.awd.provider.service;

import com.awd.provider.dto.InterventionZoneRequest;
import com.awd.provider.dto.InterventionZoneResponse;
import com.awd.provider.entity.InterventionZone;
import com.awd.provider.exception.ResourceNotFoundException;
import com.awd.provider.mapper.InterventionZoneMapper;
import com.awd.provider.repository.InterventionZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InterventionZoneService {

    private final InterventionZoneRepository zoneRepository;
    private final ProviderService providerService;
    private final InterventionZoneMapper mapper;

    /** addZone() */
    public InterventionZoneResponse addZone(Long providerId, InterventionZoneRequest request) {
        InterventionZone zone = new InterventionZone();
        zone.setProvider(providerService.findOrThrow(providerId));
        mapper.apply(zone, request);
        return mapper.toResponse(zoneRepository.save(zone));
    }

    @Transactional(readOnly = true)
    public List<InterventionZoneResponse> getByProvider(Long providerId) {
        providerService.findOrThrow(providerId);
        return zoneRepository.findByProviderId(providerId).stream().map(mapper::toResponse).toList();
    }

    /** updateZone() */
    public InterventionZoneResponse updateZone(Long providerId, Long zoneId, InterventionZoneRequest request) {
        InterventionZone zone = findForProvider(providerId, zoneId);
        mapper.apply(zone, request);
        return mapper.toResponse(zoneRepository.save(zone));
    }

    /** removeZone() */
    public void removeZone(Long providerId, Long zoneId) {
        zoneRepository.delete(findForProvider(providerId, zoneId));
    }

    private InterventionZone findForProvider(Long providerId, Long zoneId) {
        providerService.findOrThrow(providerId);
        return zoneRepository.findByIdAndProviderId(zoneId, providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Zone " + zoneId
                        + " introuvable pour le prestataire " + providerId));
    }
}
