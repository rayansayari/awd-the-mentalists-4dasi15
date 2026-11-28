package com.reserv.catalogue.controller;

import com.reserv.catalogue.dto.ServiceRequest;
import com.reserv.catalogue.dto.ServiceResponse;
import com.reserv.catalogue.dto.ServiceStatusUpdateRequest;
import com.reserv.catalogue.service.CatalogueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controleur REST pour la gestion et la consultation des prestations du catalogue.
 */
@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final CatalogueService catalogueService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceResponse> createService(@Valid @RequestBody ServiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogueService.createService(request));
    }

    @GetMapping
    public ResponseEntity<List<ServiceResponse>> getServices(
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "false") boolean includeUnavailable
    ) {
        return ResponseEntity.ok(catalogueService.getServices(minPrice, maxPrice, includeUnavailable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable Long id) {
        return ResponseEntity.ok(catalogueService.getServiceById(id));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ServiceResponse>> getServicesByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "false") boolean includeUnavailable
    ) {
        return ResponseEntity.ok(catalogueService.getServicesByCategory(categoryId, includeUnavailable));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ServiceResponse>> searchServicesByName(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "false") boolean includeUnavailable
    ) {
        return ResponseEntity.ok(catalogueService.searchServicesByName(name, includeUnavailable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceResponse> updateService(
            @PathVariable Long id,
            @Valid @RequestBody ServiceRequest request
    ) {
        return ResponseEntity.ok(catalogueService.updateService(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceResponse> updateServiceStatus(
            @PathVariable Long id,
            @Valid @RequestBody ServiceStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(catalogueService.updateServiceStatus(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        catalogueService.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}
