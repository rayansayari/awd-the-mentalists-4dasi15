package com.awd.provider.controller;

import com.awd.provider.dto.InterventionZoneRequest;
import com.awd.provider.dto.InterventionZoneResponse;
import com.awd.provider.service.InterventionZoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/providers/{providerId}/zones")
@RequiredArgsConstructor
public class InterventionZoneController {

    private final InterventionZoneService service;

    @PostMapping
    public ResponseEntity<InterventionZoneResponse> add(@PathVariable Long providerId,
                                                        @Valid @RequestBody InterventionZoneRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addZone(providerId, request));
    }

    @GetMapping
    public List<InterventionZoneResponse> getAll(@PathVariable Long providerId) {
        return service.getByProvider(providerId);
    }

    @PutMapping("/{zoneId}")
    public InterventionZoneResponse update(@PathVariable Long providerId, @PathVariable Long zoneId,
                                           @Valid @RequestBody InterventionZoneRequest request) {
        return service.updateZone(providerId, zoneId, request);
    }

    @DeleteMapping("/{zoneId}")
    public ResponseEntity<Void> remove(@PathVariable Long providerId, @PathVariable Long zoneId) {
        service.removeZone(providerId, zoneId);
        return ResponseEntity.noContent().build();
    }
}
