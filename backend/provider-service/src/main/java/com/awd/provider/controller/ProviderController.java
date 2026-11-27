package com.awd.provider.controller;

import com.awd.provider.dto.ProviderRequest;
import com.awd.provider.dto.ProviderResponse;
import com.awd.provider.service.ProviderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;

    @PostMapping
    public ResponseEntity<ProviderResponse> create(@Valid @RequestBody ProviderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(providerService.createProfile(request));
    }

    @GetMapping
    public List<ProviderResponse> getAll() {
        return providerService.getAll();
    }

    @GetMapping("/{id}")
    public ProviderResponse getById(@PathVariable Long id) {
        return providerService.getProfile(id);
    }

    @PutMapping("/{id}")
    public ProviderResponse update(@PathVariable Long id, @Valid @RequestBody ProviderRequest request) {
        return providerService.updateProfile(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        providerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    public ProviderResponse activate(@PathVariable Long id) {
        return providerService.activate(id);
    }

    @PatchMapping("/{id}/suspend")
    public ProviderResponse suspend(@PathVariable Long id) {
        return providerService.suspend(id);
    }
}
