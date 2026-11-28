package com.reserv.review.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Note moyenne et nombre d'avis publiés d'un professionnel.
 */
@Entity
@Table(name = "provider_rating_summaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProviderRatingSummary {

    @Id
    @Column(name = "provider_id")
    @EqualsAndHashCode.Include
    private Long providerId;

    @Column(name = "average_rating", nullable = false, precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(name = "rating_count", nullable = false)
    private Integer ratingCount;

    /**
     * Recalcule la moyenne à partir de la somme des notes et du nombre d'avis publiés.
     */
    public void recomputeAverage(Double average, long count) {
        this.ratingCount = (int) count;
        if (count == 0 || average == null) {
            this.averageRating = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else {
            this.averageRating = BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP);
        }
    }
}
