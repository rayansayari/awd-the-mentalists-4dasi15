package com.awd.provider.controller;

import com.awd.provider.dto.AvailabilityRequest;
import com.awd.provider.dto.AvailabilityResponse;
import com.awd.provider.service.AvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/providers/{providerId}/availabilities")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService service;

    @PostMapping
    public ResponseEntity<AvailabilityResponse> add(@PathVariable Long providerId,
                                                    @Valid @RequestBody AvailabilityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addAvailability(providerId, request));
    }

    @GetMapping
    public List<AvailabilityResponse> getAll(@PathVariable Long providerId) {
        return service.getByProvider(providerId);
    }

    @PutMapping("/{availabilityId}")
    public AvailabilityResponse update(@PathVariable Long providerId, @PathVariable Long availabilityId,
                                       @Valid @RequestBody AvailabilityRequest request) {
        return service.updateAvailability(providerId, availabilityId, request);
    }

    @DeleteMapping("/{availabilityId}")
    public ResponseEntity<Void> delete(@PathVariable Long providerId, @PathVariable Long availabilityId) {
        service.deleteAvailability(providerId, availabilityId);
        return ResponseEntity.noContent().build();
    }
}
