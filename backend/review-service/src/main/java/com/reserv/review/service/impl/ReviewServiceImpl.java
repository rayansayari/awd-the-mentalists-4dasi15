package com.reserv.review.service.impl;

import com.reserv.review.dto.EligibilityResponse;
import com.reserv.review.dto.RatingSummaryResponse;
import com.reserv.review.dto.ReviewCreateRequest;
import com.reserv.review.dto.ReviewResponse;
import com.reserv.review.dto.ReviewUpdateRequest;
import com.reserv.review.entity.ProcessedEvent;
import com.reserv.review.entity.ProviderRatingSummary;
import com.reserv.review.entity.Review;
import com.reserv.review.entity.ReviewStatus;
import com.reserv.review.entity.ReviewableBooking;
import com.reserv.review.event.ReviewEventPublisher;
import com.reserv.review.exception.ForbiddenException;
import com.reserv.review.exception.InvalidReviewException;
import com.reserv.review.exception.ResourceNotFoundException;
import com.reserv.review.exception.ReviewConflictException;
import com.reserv.review.exception.UnauthorizedException;
import com.reserv.review.repository.ProcessedEventRepository;
import com.reserv.review.repository.ProviderRatingSummaryRepository;
import com.reserv.review.repository.ReviewRepository;
import com.reserv.review.repository.ReviewableBookingRepository;
import com.reserv.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implémentation de la logique métier du Review Service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private static final String ADMIN_ROLE = "ADMIN";

    private final ReviewRepository reviewRepository;
    private final ReviewableBookingRepository reviewableBookingRepository;
    private final ProviderRatingSummaryRepository summaryRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final ReviewEventPublisher eventPublisher;

    @Override
    @Transactional
    public ReviewResponse createReview(Long userId, ReviewCreateRequest request) {
        requireAuthenticated(userId);
        validateRating(request.getRating());

        // Réservation inconnue = prestation non terminée (aucun booking.completed reçu) -> 403
        ReviewableBooking reviewable = reviewableBookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ForbiddenException(
                        "La réservation n'est pas terminée : l'avis n'est pas encore autorisé"));

        if (!reviewable.getClientId().equals(userId)) {
            throw new ForbiddenException("Cette réservation n'appartient pas au client authentifié");
        }
        if (!reviewable.getProviderId().equals(request.getProviderId())) {
            throw new InvalidReviewException("Le prestataire ne correspond pas à la réservation");
        }
        if (Boolean.TRUE.equals(reviewable.getReviewed()) || reviewRepository.existsByBookingId(request.getBookingId())) {
            throw new ReviewConflictException("Un avis a déjà été publié pour cette réservation");
        }

        Review review = Review.builder()
                .bookingId(reviewable.getBookingId())
                .clientId(reviewable.getClientId())
                .providerId(reviewable.getProviderId())
                .rating(request.getRating())
                .comment(normalizeComment(request.getComment()))
                .status(ReviewStatus.PUBLISHED)
                .build();
        Review saved = reviewRepository.save(review);

        reviewable.markAsReviewed();
        reviewableBookingRepository.save(reviewable);

        recomputeProviderRating(saved.getProviderId());
        return toResponse(saved);
    }

    @Override
    public ReviewResponse getReviewById(Long id) {
        Review review = findReview(id);
        if (review.getStatus() != ReviewStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Avis introuvable avec l'identifiant " + id);
        }
        return toResponse(review);
    }

    @Override
    public List<ReviewResponse> getReviewsByProvider(Long providerId) {
        List<Review> reviews = reviewRepository.findByProviderIdAndStatusOrderByCreatedAtDesc(
                providerId, ReviewStatus.PUBLISHED);
        if (reviews.isEmpty() && !summaryRepository.existsById(providerId)) {
            throw new ResourceNotFoundException("Aucun avis pour le professionnel " + providerId);
        }
        return reviews.stream().map(this::toResponse).toList();
    }

    @Override
    public RatingSummaryResponse getProviderSummary(Long providerId) {
        ProviderRatingSummary summary = summaryRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucune note pour le professionnel " + providerId));
        return RatingSummaryResponse.builder()
                .providerId(summary.getProviderId())
                .averageRating(summary.getAverageRating())
                .ratingCount(summary.getRatingCount())
                .build();
    }

    @Override
    public List<ReviewResponse> getReviewsByClient(Long clientId, Long userId, String role) {
        requireAuthenticated(userId);
        if (!isAdmin(role) && !clientId.equals(userId)) {
            throw new ForbiddenException("Vous ne pouvez consulter que vos propres avis");
        }
        return reviewRepository.findByClientIdAndStatusOrderByCreatedAtDesc(clientId, ReviewStatus.PUBLISHED)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public EligibilityResponse checkEligibility(Long bookingId, Long userId) {
        requireAuthenticated(userId);
        ReviewableBooking reviewable = reviewableBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Réservation " + bookingId + " introuvable ou prestation non terminée"));

        boolean eligible = true;
        String reason = "La réservation peut être évaluée";
        if (!reviewable.getClientId().equals(userId)) {
            eligible = false;
            reason = "La réservation n'appartient pas au client authentifié";
        } else if (Boolean.TRUE.equals(reviewable.getReviewed())) {
            eligible = false;
            reason = "Un avis a déjà été publié pour cette réservation";
        }
        return EligibilityResponse.builder()
                .bookingId(bookingId)
                .eligible(eligible)
                .reason(reason)
                .build();
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long id, Long userId, ReviewUpdateRequest request) {
        requireAuthenticated(userId);
        validateRating(request.getRating());

        Review review = findReview(id);
        if (review.getStatus() != ReviewStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Avis introuvable avec l'identifiant " + id);
        }
        if (!review.getClientId().equals(userId)) {
            throw new ForbiddenException("Seul l'auteur peut modifier son avis");
        }

        review.setRating(request.getRating());
        review.setComment(normalizeComment(request.getComment()));
        Review saved = reviewRepository.save(review);

        recomputeProviderRating(saved.getProviderId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteReview(Long id, Long userId, String role) {
        requireAuthenticated(userId);

        Review review = findReview(id);
        if (review.getStatus() == ReviewStatus.DELETED) {
            throw new ResourceNotFoundException("Avis introuvable avec l'identifiant " + id);
        }
        if (!isAdmin(role) && !review.getClientId().equals(userId)) {
            throw new ForbiddenException("Seul l'auteur (ou un administrateur) peut supprimer cet avis");
        }

        review.setStatus(ReviewStatus.DELETED);
        reviewRepository.save(review);

        recomputeProviderRating(review.getProviderId());
    }

    @Override
    @Transactional
    public ReviewResponse hideReview(Long id, String role) {
        if (!isAdmin(role)) {
            throw new ForbiddenException("Seul un administrateur peut masquer un avis");
        }

        Review review = findReview(id);
        if (review.getStatus() == ReviewStatus.DELETED) {
            throw new ResourceNotFoundException("Avis introuvable avec l'identifiant " + id);
        }

        review.setStatus(ReviewStatus.HIDDEN);
        Review saved = reviewRepository.save(review);

        recomputeProviderRating(saved.getProviderId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void registerCompletedBooking(String eventId, Long bookingId, Long clientId, Long providerId) {
        if (eventId != null && processedEventRepository.existsById(eventId)) {
            log.info("Événement déjà traité, ignoré (eventId={})", eventId);
            return;
        }

        if (!reviewableBookingRepository.existsById(bookingId)) {
            reviewableBookingRepository.save(ReviewableBooking.builder()
                    .bookingId(bookingId)
                    .clientId(clientId)
                    .providerId(providerId)
                    .completedAt(LocalDateTime.now())
                    .reviewed(Boolean.FALSE)
                    .build());
            log.info("Réservation {} enregistrée comme évaluable (client={}, prestataire={})",
                    bookingId, clientId, providerId);
        }

        if (eventId != null) {
            processedEventRepository.save(ProcessedEvent.builder()
                    .eventId(eventId)
                    .processedAt(LocalDateTime.now())
                    .build());
        }
    }

    // ------------------------------------------------------------------ helpers

    /**
     * Recalcule la note moyenne (avis PUBLISHED uniquement) et publie review.rating-updated.
     */
    private void recomputeProviderRating(Long providerId) {
        long count = reviewRepository.countByProviderIdAndStatus(providerId, ReviewStatus.PUBLISHED);
        Double average = reviewRepository.averageRatingByProviderIdAndStatus(providerId, ReviewStatus.PUBLISHED);

        ProviderRatingSummary summary = summaryRepository.findById(providerId)
                .orElseGet(() -> ProviderRatingSummary.builder().providerId(providerId).build());
        summary.recomputeAverage(average, count);
        ProviderRatingSummary saved = summaryRepository.save(summary);

        eventPublisher.publishRatingUpdated(saved);
    }

    private Review findReview(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Avis introuvable avec l'identifiant " + id));
    }

    private void requireAuthenticated(Long userId) {
        if (userId == null) {
            throw new UnauthorizedException("Authentification requise");
        }
    }

    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new InvalidReviewException("La note doit être comprise entre 1 et 5");
        }
    }

    private boolean isAdmin(String role) {
        return ADMIN_ROLE.equalsIgnoreCase(role);
    }

    private String normalizeComment(String comment) {
        return (comment == null || comment.isBlank()) ? null : comment.trim();
    }

    private ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .bookingId(review.getBookingId())
                .clientId(review.getClientId())
                .providerId(review.getProviderId())
                .rating(review.getRating())
                .comment(review.getComment())
                .status(review.getStatus())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
