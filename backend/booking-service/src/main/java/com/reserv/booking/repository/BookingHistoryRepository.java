package com.reserv.booking.repository;

import com.reserv.booking.entity.BookingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository JPA pour l'historique des changements de statut des réservations.
 */
@Repository
public interface BookingHistoryRepository extends JpaRepository<BookingHistory, Long> {

    List<BookingHistory> findByBookingIdOrderByChangedAtAsc(Long bookingId);
}
