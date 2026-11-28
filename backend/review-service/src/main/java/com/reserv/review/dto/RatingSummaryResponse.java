package com.reserv.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO de réponse : note moyenne et nombre d'avis d'un professionnel.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingSummaryResponse {

    private Long providerId;
    private BigDecimal averageRating;
    private Integer ratingCount;
}
