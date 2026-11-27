package com.awd.provider.entity;

import com.awd.provider.enums.ProviderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Prestataire. userId vient du User Service (pas d'entite User ici).
 * Un Provider possede plusieurs ProviderSkill, InterventionZone et Availability.
 */
@Entity
@Table(name = "providers",
        uniqueConstraints = @UniqueConstraint(name = "uk_provider_email", columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identifiant de l'utilisateur dans le User Service (simple Long, pas de relation JPA). */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(length = 1000)
    private String description;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProviderStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // cascade ALL + orphanRemoval : supprimer le Provider supprime ses enfants
    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProviderSkill> providerSkills = new ArrayList<>();

    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InterventionZone> zones = new ArrayList<>();

    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Availability> availabilities = new ArrayList<>();

    /** Methode metier du diagramme : passe le prestataire en ACTIVE. */
    public void activate() {
        this.status = ProviderStatus.ACTIVE;
    }

    /** Methode metier du diagramme : passe le prestataire en SUSPENDED. */
    public void suspend() {
        this.status = ProviderStatus.SUSPENDED;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.status == null) {
            this.status = ProviderStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
