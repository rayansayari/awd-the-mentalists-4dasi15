package com.reserv.review.repository;

import com.reserv.review.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository JPA des événements déjà traités (idempotence).
 */
@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {
}
