package com.reserv.review.event;

import com.reserv.review.entity.ProviderRatingSummary;

/**
 * Contrat de publication des événements métier du Review Service.
 */
public interface ReviewEventPublisher {

    /**
     * Publie review.rating-updated : nouvelle note moyenne d'un professionnel (consommé par le Provider Service).
     */
    void publishRatingUpdated(ProviderRatingSummary summary);
}
