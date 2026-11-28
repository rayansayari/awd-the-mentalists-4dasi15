package com.reserv.review.controller;

import com.reserv.review.dto.EligibilityResponse;
import com.reserv.review.dto.RatingSummaryResponse;
import com.reserv.review.dto.ReviewCreateRequest;
import com.reserv.review.dto.ReviewResponse;
import com.reserv.review.dto.ReviewUpdateRequest;
import com.reserv.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API REST du Review Service.
 * <p>
 * L'identité de l'utilisateur authentifié est transmise par l'API Gateway (après validation du JWT)
 * via les en-têtes X-User-Id et X-User-Role.
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @RequestHeader(value = USER_ID_HEADER, required = false) Long userId,
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        ReviewResponse created = reviewService.createReview(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByProvider(@PathVariable Long providerId) {
        return ResponseEntity.ok(reviewService.getReviewsByProvider(providerId));
    }

    @GetMapping("/provider/{providerId}/summary")
    public ResponseEntity<RatingSummaryResponse> getProviderSummary(@PathVariable Long providerId) {
        return ResponseEntity.ok(reviewService.getProviderSummary(providerId));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByClient(
            @PathVariable Long clientId,
            @RequestHeader(value = USER_ID_HEADER, required = false) Long userId,
            @RequestHeader(value = USER_ROLE_HEADER, required = false) String role
    ) {
        return ResponseEntity.ok(reviewService.getReviewsByClient(clientId, userId, role));
    }

    @GetMapping("/booking/{bookingId}/eligibility")
    public ResponseEntity<EligibilityResponse> checkEligibility(
            @PathVariable Long bookingId,
            @RequestHeader(value = USER_ID_HEADER, required = false) Long userId
    ) {
        return ResponseEntity.ok(reviewService.checkEligibility(bookingId, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long id,
            @RequestHeader(value = USER_ID_HEADER, required = false) Long userId,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        return ResponseEntity.ok(reviewService.updateReview(id, userId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long id,
            @RequestHeader(value = USER_ID_HEADER, required = false) Long userId,
            @RequestHeader(value = USER_ROLE_HEADER, required = false) String role
    ) {
        reviewService.deleteReview(id, userId, role);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/hide")
    public ResponseEntity<ReviewResponse> hideReview(
            @PathVariable Long id,
            @RequestHeader(value = USER_ROLE_HEADER, required = false) String role
    ) {
        return ResponseEntity.ok(reviewService.hideReview(id, role));
    }
}
