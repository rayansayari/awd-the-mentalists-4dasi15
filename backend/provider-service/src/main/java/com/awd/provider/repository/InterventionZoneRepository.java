package com.awd.provider.repository;

import com.awd.provider.entity.InterventionZone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InterventionZoneRepository extends JpaRepository<InterventionZone, Long> {
    List<InterventionZone> findByProviderId(Long providerId);
    Optional<InterventionZone> findByIdAndProviderId(Long id, Long providerId);
}
