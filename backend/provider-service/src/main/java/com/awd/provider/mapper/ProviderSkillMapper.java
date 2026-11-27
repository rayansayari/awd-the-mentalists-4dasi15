package com.awd.provider.mapper;

import com.awd.provider.dto.ProviderSkillResponse;
import com.awd.provider.entity.ProviderSkill;
import org.springframework.stereotype.Component;

@Component
public class ProviderSkillMapper {
    public ProviderSkillResponse toResponse(ProviderSkill ps) {
        return new ProviderSkillResponse(ps.getId(), ps.getProvider().getId(),
                ps.getSkill().getId(), ps.getSkill().getName(), ps.getExperienceLevel());
    }
}
