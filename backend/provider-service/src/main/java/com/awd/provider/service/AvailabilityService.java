package com.awd.provider.service;

import com.awd.provider.dto.AvailabilityRequest;
import com.awd.provider.dto.AvailabilityResponse;
import com.awd.provider.entity.Availability;
import com.awd.provider.exception.BadRequestException;
import com.awd.provider.exception.ResourceNotFoundException;
import com.awd.provider.mapper.AvailabilityMapper;
import com.awd.provider.repository.AvailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final ProviderService providerService;
    private final AvailabilityMapper mapper;

    /** addAvailability() */
    public AvailabilityResponse addAvailability(Long providerId, AvailabilityRequest request) {
        validateTimes(request);
        Availability a = new Availability();
        a.setProvider(providerService.findOrThrow(providerId));
        mapper.apply(a, request);
        return mapper.toResponse(availabilityRepository.save(a));
    }

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getByProvider(Long providerId) {
        providerService.findOrThrow(providerId);
        return availabilityRepository.findByProviderId(providerId).stream().map(mapper::toResponse).toList();
    }

    /** updateAvailability() */
    public AvailabilityResponse updateAvailability(Long providerId, Long availabilityId, AvailabilityRequest request) {
        validateTimes(request);
        Availability a = findForProvider(providerId, availabilityId);
        mapper.apply(a, request);
        return mapper.toResponse(availabilityRepository.save(a));
    }

    /** deleteAvailability() */
    public void deleteAvailability(Long providerId, Long availabilityId) {
        availabilityRepository.delete(findForProvider(providerId, availabilityId));
    }

    /** Regle metier : heure de debut strictement inferieure a l'heure de fin. */
    private void validateTimes(AvailabilityRequest r) {
        if (!r.startTime().isBefore(r.endTime())) {
            throw new BadRequestException("startTime doit etre strictement inferieure a endTime");
        }
    }

    private Availability findForProvider(Long providerId, Long availabilityId) {
        providerService.findOrThrow(providerId);
        return availabilityRepository.findByIdAndProviderId(availabilityId, providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Disponibilite " + availabilityId
                        + " introuvable pour le prestataire " + providerId));
    }
}
