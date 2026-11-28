package com.reserv.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de réponse : une réservation peut-elle être évaluée par l'utilisateur courant ?
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EligibilityResponse {

    private Long bookingId;
    private boolean eligible;
    private String reason;
}
