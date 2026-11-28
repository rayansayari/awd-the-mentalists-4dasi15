package com.reserv.review.repository;

import com.reserv.review.entity.ReviewableBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository JPA des réservations évaluables.
 */
@Repository
public interface ReviewableBookingRepository extends JpaRepository<ReviewableBooking, Long> {
}
