package com.awd.provider.controller;

import com.awd.provider.dto.SkillRequest;
import com.awd.provider.dto.SkillResponse;
import com.awd.provider.service.SkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    @PostMapping
    public ResponseEntity<SkillResponse> create(@Valid @RequestBody SkillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(skillService.createSkill(request));
    }

    @GetMapping
    public List<SkillResponse> getAll() {
        return skillService.getAll();
    }

    @GetMapping("/{id}")
    public SkillResponse getById(@PathVariable Long id) {
        return skillService.getById(id);
    }

    @PutMapping("/{id}")
    public SkillResponse update(@PathVariable Long id, @Valid @RequestBody SkillRequest request) {
        return skillService.updateSkill(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        skillService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
