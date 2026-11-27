package com.awd.provider.mapper;

import com.awd.provider.dto.SkillRequest;
import com.awd.provider.dto.SkillResponse;
import com.awd.provider.entity.Skill;
import org.springframework.stereotype.Component;

@Component
public class SkillMapper {
    public Skill toEntity(SkillRequest r) {
        Skill s = new Skill();
        apply(s, r);
        return s;
    }
    public void apply(Skill s, SkillRequest r) {
        s.setName(r.name());
        s.setDescription(r.description());
    }
    public SkillResponse toResponse(Skill s) {
        return new SkillResponse(s.getId(), s.getName(), s.getDescription());
    }
}
