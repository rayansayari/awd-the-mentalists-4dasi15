package com.awd.provider.controller;

import com.awd.provider.dto.ProviderSkillRequest;
import com.awd.provider.dto.ProviderSkillResponse;
import com.awd.provider.service.ProviderSkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/providers/{providerId}/skills")
@RequiredArgsConstructor
public class ProviderSkillController {

    private final ProviderSkillService service;

    @PostMapping
    public ResponseEntity<ProviderSkillResponse> add(@PathVariable Long providerId,
                                                     @Valid @RequestBody ProviderSkillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addSkill(providerId, request));
    }

    @GetMapping
    public List<ProviderSkillResponse> getAll(@PathVariable Long providerId) {
        return service.getByProvider(providerId);
    }

    @DeleteMapping("/{skillId}")
    public ResponseEntity<Void> remove(@PathVariable Long providerId, @PathVariable Long skillId) {
        service.removeSkill(providerId, skillId);
        return ResponseEntity.noContent().build();
    }
}
