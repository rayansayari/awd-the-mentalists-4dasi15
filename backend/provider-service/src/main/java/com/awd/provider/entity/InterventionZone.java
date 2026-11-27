package com.awd.provider.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Zone d'intervention d'un prestataire. */
@Entity
@Table(name = "intervention_zones")
@Getter
@Setter
@NoArgsConstructor
public class InterventionZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 100)
    private String region;

    @Column(name = "postal_code", length = 10)
    private String postalCode;
}
