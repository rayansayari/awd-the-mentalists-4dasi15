package com.reserv.review.repository;

import com.reserv.review.entity.ProviderRatingSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository JPA des notes moyennes par professionnel.
 */
@Repository
public interface ProviderRatingSummaryRepository extends JpaRepository<ProviderRatingSummary, Long> {
}
