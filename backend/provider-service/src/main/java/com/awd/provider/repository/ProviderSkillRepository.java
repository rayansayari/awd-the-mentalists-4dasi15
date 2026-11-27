package com.awd.provider.repository;

import com.awd.provider.entity.ProviderSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProviderSkillRepository extends JpaRepository<ProviderSkill, Long> {
    List<ProviderSkill> findByProviderId(Long providerId);
    Optional<ProviderSkill> findByProviderIdAndSkillId(Long providerId, Long skillId);
    boolean existsByProviderIdAndSkillId(Long providerId, Long skillId);
    boolean existsBySkillId(Long skillId);
}
