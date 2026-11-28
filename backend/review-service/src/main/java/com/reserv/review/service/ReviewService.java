package com.reserv.review.service;

import com.reserv.review.dto.EligibilityResponse;
import com.reserv.review.dto.RatingSummaryResponse;
import com.reserv.review.dto.ReviewCreateRequest;
import com.reserv.review.dto.ReviewResponse;
import com.reserv.review.dto.ReviewUpdateRequest;

import java.util.List;

/**
 * Contrat de la logique métier du Review Service.
 * L'identité (userId) et le rôle sont fournis par la couche d'authentification (API Gateway / JWT).
 */
public interface ReviewService {

    ReviewResponse createReview(Long userId, ReviewCreateRequest request);

    ReviewResponse getReviewById(Long id);

    List<ReviewResponse> getReviewsByProvider(Long providerId);

    RatingSummaryResponse getProviderSummary(Long providerId);

    List<ReviewResponse> getReviewsByClient(Long clientId, Long userId, String role);

    EligibilityResponse checkEligibility(Long bookingId, Long userId);

    ReviewResponse updateReview(Long id, Long userId, ReviewUpdateRequest request);

    void deleteReview(Long id, Long userId, String role);

    ReviewResponse hideReview(Long id, String role);

    /**
     * Enregistre une réservation terminée (événement booking.completed) comme évaluable. Idempotent.
     */
    void registerCompletedBooking(String eventId, Long bookingId, Long clientId, Long providerId);
}
