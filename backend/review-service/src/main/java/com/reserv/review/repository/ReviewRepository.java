package com.reserv.review.repository;

import com.reserv.review.entity.Review;
import com.reserv.review.entity.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository JPA pour la gestion des avis.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProviderIdAndStatusOrderByCreatedAtDesc(Long providerId, ReviewStatus status);

    List<Review> findByClientIdAndStatusOrderByCreatedAtDesc(Long clientId, ReviewStatus status);

    boolean existsByBookingId(Long bookingId);

    long countByProviderIdAndStatus(Long providerId, ReviewStatus status);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.providerId = :providerId AND r.status = :status")
    Double averageRatingByProviderIdAndStatus(@Param("providerId") Long providerId,
                                              @Param("status") ReviewStatus status);
}
