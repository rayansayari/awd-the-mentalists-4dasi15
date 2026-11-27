package com.awd.provider.repository;

import com.awd.provider.entity.Availability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    List<Availability> findByProviderId(Long providerId);
    Optional<Availability> findByIdAndProviderId(Long id, Long providerId);
}
