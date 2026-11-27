package com.awd.provider.service;

import com.awd.provider.dto.ProviderSkillRequest;
import com.awd.provider.dto.ProviderSkillResponse;
import com.awd.provider.entity.Provider;
import com.awd.provider.entity.ProviderSkill;
import com.awd.provider.entity.Skill;
import com.awd.provider.exception.ConflictException;
import com.awd.provider.exception.ResourceNotFoundException;
import com.awd.provider.mapper.ProviderSkillMapper;
import com.awd.provider.repository.ProviderSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProviderSkillService {

    private final ProviderSkillRepository providerSkillRepository;
    private final ProviderService providerService;
    private final SkillService skillService;
    private final ProviderSkillMapper mapper;

    /** addSkill() : associe une competence a un prestataire. */
    public ProviderSkillResponse addSkill(Long providerId, ProviderSkillRequest request) {
        Provider provider = providerService.findOrThrow(providerId);
        Skill skill = skillService.findOrThrow(request.skillId());
        if (providerSkillRepository.existsByProviderIdAndSkillId(providerId, request.skillId())) {
            throw new ConflictException("Le prestataire " + providerId
                    + " possede deja la competence " + request.skillId());
        }
        ProviderSkill ps = new ProviderSkill();
        ps.setProvider(provider);
        ps.setSkill(skill);
        ps.setExperienceLevel(request.experienceLevel());
        return mapper.toResponse(providerSkillRepository.save(ps));
    }

    @Transactional(readOnly = true)
    public List<ProviderSkillResponse> getByProvider(Long providerId) {
        providerService.findOrThrow(providerId);
        return providerSkillRepository.findByProviderId(providerId).stream().map(mapper::toResponse).toList();
    }

    /** removeSkill() : skillId = id de la Skill (pas de la ligne d'association). */
    public void removeSkill(Long providerId, Long skillId) {
        providerService.findOrThrow(providerId);
        ProviderSkill ps = providerSkillRepository.findByProviderIdAndSkillId(providerId, skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Le prestataire " + providerId
                        + " ne possede pas la competence " + skillId));
        providerSkillRepository.delete(ps);
    }
}
