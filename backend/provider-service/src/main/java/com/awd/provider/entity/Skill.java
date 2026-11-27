package com.awd.provider.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** Competence (ex: Plomberie). Une Skill peut etre associee a plusieurs ProviderSkill. */
@Entity
@Table(name = "skills",
        uniqueConstraints = @UniqueConstraint(name = "uk_skill_name", columnNames = "name"))
@Getter
@Setter
@NoArgsConstructor
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    // Pas de cascade : supprimer une Skill utilisee est refuse (409) par le service
    @OneToMany(mappedBy = "skill")
    private List<ProviderSkill> providerSkills = new ArrayList<>();
}
