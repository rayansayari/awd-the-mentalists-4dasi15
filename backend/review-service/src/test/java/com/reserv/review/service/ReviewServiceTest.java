package com.reserv.review.service;

import com.reserv.review.dto.ReviewCreateRequest;
import com.reserv.review.dto.ReviewResponse;
import com.reserv.review.entity.ProviderRatingSummary;
import com.reserv.review.entity.Review;
import com.reserv.review.entity.ReviewStatus;
import com.reserv.review.entity.ReviewableBooking;
import com.reserv.review.event.ReviewEventPublisher;
import com.reserv.review.exception.ForbiddenException;
import com.reserv.review.exception.InvalidReviewException;
import com.reserv.review.exception.ReviewConflictException;
import com.reserv.review.exception.UnauthorizedException;
import com.reserv.review.repository.ProcessedEventRepository;
import com.reserv.review.repository.ProviderRatingSummaryRepository;
import com.reserv.review.repository.ReviewRepository;
import com.reserv.review.repository.ReviewableBookingRepository;
import com.reserv.review.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    private static final Long CLIENT_ID = 7L;
    private static final Long PROVIDER_ID = 15L;
    private static final Long BOOKING_ID = 42L;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewableBookingRepository reviewableBookingRepository;

    @Mock
    private ProviderRatingSummaryRepository summaryRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Mock
    private ReviewEventPublisher eventPublisher;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private ReviewableBooking reviewable;
    private ReviewCreateRequest request;

    @BeforeEach
    void setUp() {
        reviewable = ReviewableBooking.builder()
                .bookingId(BOOKING_ID)
                .clientId(CLIENT_ID)
                .providerId(PROVIDER_ID)
                .completedAt(LocalDateTime.now())
                .reviewed(Boolean.FALSE)
                .build();

        request = ReviewCreateRequest.builder()
                .bookingId(BOOKING_ID)
                .providerId(PROVIDER_ID)
                .rating(5)
                .comment("Prestation impeccable et ponctuelle.")
                .build();
    }

    @Test
    @DisplayName("Publier un avis sur une réservation terminée recalcule la moyenne et publie l'événement")
    void createReview_success() {
        when(reviewableBookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(reviewable));
        when(reviewRepository.existsByBookingId(BOOKING_ID)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(11L);
            review.setCreatedAt(LocalDateTime.now());
            return review;
        });
        when(reviewRepository.countByProviderIdAndStatus(PROVIDER_ID, ReviewStatus.PUBLISHED)).thenReturn(1L);
        when(reviewRepository.averageRatingByProviderIdAndStatus(PROVIDER_ID, ReviewStatus.PUBLISHED)).thenReturn(5.0);
        when(summaryRepository.findById(PROVIDER_ID)).thenReturn(Optional.empty());
        when(summaryRepository.save(any(ProviderRatingSummary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewResponse response = reviewService.createReview(CLIENT_ID, request);

        assertEquals(11L, response.getId());
        assertEquals(CLIENT_ID, response.getClientId());
        assertEquals(ReviewStatus.PUBLISHED, response.getStatus());
        assertTrue(reviewable.getReviewed());
        verify(eventPublisher).publishRatingUpdated(any(ProviderRatingSummary.class));
    }

    @Test
    @DisplayName("Un avis est refusé (403) si la réservation n'est pas terminée")
    void createReview_bookingNotCompleted_forbidden() {
        when(reviewableBookingRepository.findById(BOOKING_ID)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> reviewService.createReview(CLIENT_ID, request));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Un avis est refusé (403) si la réservation appartient à un autre client")
    void createReview_notOwner_forbidden() {
        when(reviewableBookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(reviewable));

        assertThrows(ForbiddenException.class, () -> reviewService.createReview(99L, request));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Un second avis pour la même réservation est refusé (409)")
    void createReview_alreadyReviewed_conflict() {
        reviewable.markAsReviewed();
        when(reviewableBookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(reviewable));

        assertThrows(ReviewConflictException.class, () -> reviewService.createReview(CLIENT_ID, request));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Une note hors de 1..5 est refusée (400)")
    void createReview_invalidRating_badRequest() {
        request.setRating(6);

        assertThrows(InvalidReviewException.class, () -> reviewService.createReview(CLIENT_ID, request));
    }

    @Test
    @DisplayName("Sans utilisateur authentifié, la publication est refusée (401)")
    void createReview_unauthenticated() {
        assertThrows(UnauthorizedException.class, () -> reviewService.createReview(null, request));
    }

    @Test
    @DisplayName("Seul un administrateur peut masquer un avis (403 sinon)")
    void hideReview_nonAdmin_forbidden() {
        assertThrows(ForbiddenException.class, () -> reviewService.hideReview(11L, "CLIENT"));
    }

    @Test
    @DisplayName("booking.completed rejoué (même eventId) n'est traité qu'une seule fois")
    void registerCompletedBooking_idempotent() {
        when(processedEventRepository.existsById("evt-504")).thenReturn(true);

        reviewService.registerCompletedBooking("evt-504", BOOKING_ID, CLIENT_ID, PROVIDER_ID);

        verify(reviewableBookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("booking.completed crée une réservation évaluable et mémorise l'eventId")
    void registerCompletedBooking_createsReviewableBooking() {
        when(processedEventRepository.existsById("evt-504")).thenReturn(false);
        when(reviewableBookingRepository.existsById(BOOKING_ID)).thenReturn(false);

        reviewService.registerCompletedBooking("evt-504", BOOKING_ID, CLIENT_ID, PROVIDER_ID);

        verify(reviewableBookingRepository).save(any(ReviewableBooking.class));
        verify(processedEventRepository).save(any());
    }

    @Test
    @DisplayName("Supprimer le dernier avis remet la moyenne à 0 et publie l'événement")
    void deleteReview_recomputesToZero() {
        Review review = Review.builder()
                .id(11L).bookingId(BOOKING_ID).clientId(CLIENT_ID).providerId(PROVIDER_ID)
                .rating(5).status(ReviewStatus.PUBLISHED).createdAt(LocalDateTime.now())
                .build();
        when(reviewRepository.findById(11L)).thenReturn(Optional.of(review));
        when(reviewRepository.countByProviderIdAndStatus(PROVIDER_ID, ReviewStatus.PUBLISHED)).thenReturn(0L);
        when(reviewRepository.averageRatingByProviderIdAndStatus(PROVIDER_ID, ReviewStatus.PUBLISHED)).thenReturn(null);
        ProviderRatingSummary existing = ProviderRatingSummary.builder()
                .providerId(PROVIDER_ID).averageRating(new BigDecimal("5.00")).ratingCount(1).build();
        when(summaryRepository.findById(PROVIDER_ID)).thenReturn(Optional.of(existing));
        when(summaryRepository.save(any(ProviderRatingSummary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        reviewService.deleteReview(11L, CLIENT_ID, "CLIENT");

        assertEquals(ReviewStatus.DELETED, review.getStatus());
        assertEquals(0, existing.getRatingCount());
        assertEquals(0, existing.getAverageRating().compareTo(BigDecimal.ZERO));
        verify(eventPublisher).publishRatingUpdated(existing);
    }
}
