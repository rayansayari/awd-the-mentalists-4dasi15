package com.awd.provider.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Table d'association Provider <-> Skill, porteuse de experienceLevel.
 * Colonnes FK : provider_id et skill_id.
 */
@Entity
@Table(name = "provider_skills",
        uniqueConstraints = @UniqueConstraint(name = "uk_provider_skill",
                columnNames = {"provider_id", "skill_id"}))
@Getter
@Setter
@NoArgsConstructor
public class ProviderSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(name = "experience_level", nullable = false, length = 50)
    private String experienceLevel;
}
