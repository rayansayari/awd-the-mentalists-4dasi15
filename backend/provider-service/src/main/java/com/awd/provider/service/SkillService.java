package com.awd.provider.service;

import com.awd.provider.dto.SkillRequest;
import com.awd.provider.dto.SkillResponse;
import com.awd.provider.entity.Skill;
import com.awd.provider.exception.ConflictException;
import com.awd.provider.exception.ResourceNotFoundException;
import com.awd.provider.mapper.SkillMapper;
import com.awd.provider.repository.ProviderSkillRepository;
import com.awd.provider.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SkillService {

    private final SkillRepository skillRepository;
    private final ProviderSkillRepository providerSkillRepository;
    private final SkillMapper mapper;

    /** createSkill() */
    public SkillResponse createSkill(SkillRequest request) {
        if (skillRepository.existsByName(request.name())) {
            throw new ConflictException("La competence '" + request.name() + "' existe deja");
        }
        return mapper.toResponse(skillRepository.save(mapper.toEntity(request)));
    }

    @Transactional(readOnly = true)
    public List<SkillResponse> getAll() {
        return skillRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SkillResponse getById(Long id) {
        return mapper.toResponse(findOrThrow(id));
    }

    /** updateSkill() */
    public SkillResponse updateSkill(Long id, SkillRequest request) {
        Skill skill = findOrThrow(id);
        if (skillRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new ConflictException("La competence '" + request.name() + "' existe deja");
        }
        mapper.apply(skill, request);
        return mapper.toResponse(skillRepository.save(skill));
    }

    /** Refuse (409) si la competence est encore associee a un prestataire. */
    public void delete(Long id) {
        Skill skill = findOrThrow(id);
        if (providerSkillRepository.existsBySkillId(id)) {
            throw new ConflictException("Impossible de supprimer la competence " + id
                    + " : elle est associee a au moins un prestataire");
        }
        skillRepository.delete(skill);
    }

    public Skill findOrThrow(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Competence introuvable avec l'id " + id));
    }
}
