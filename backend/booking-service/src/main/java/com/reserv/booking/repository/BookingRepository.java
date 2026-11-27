package com.reserv.booking.repository;

import com.reserv.booking.entity.Booking;
import com.reserv.booking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Repository JPA pour la gestion des réservations.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByClientIdOrderByBookingDateDescStartTimeDesc(Long clientId);

    List<Booking> findByProviderIdOrderByBookingDateDescStartTimeDesc(Long providerId);

    List<Booking> findByProviderIdAndStatusNotOrderByBookingDateAscStartTimeAsc(
            Long providerId,
            BookingStatus excludedStatus
    );

    List<Booking> findByProviderIdAndBookingDateAndStatusNotOrderByStartTimeAsc(
            Long providerId,
            LocalDate bookingDate,
            BookingStatus excludedStatus
    );

    List<Booking> findByClientIdAndStatusNotOrderByBookingDateAscStartTimeAsc(
            Long clientId,
            BookingStatus excludedStatus
    );

    List<Booking> findByClientIdAndBookingDateAndStatusNotOrderByStartTimeAsc(
            Long clientId,
            LocalDate bookingDate,
            BookingStatus excludedStatus
    );

    /**
     * Vérifie si un créneau actif (non annulé) chevauche la plage horaire demandée pour un prestataire.
     */
    @Query("""
            SELECT COUNT(b) > 0 FROM Booking b
            WHERE b.providerId = :providerId
              AND b.bookingDate = :bookingDate
              AND b.status <> com.reserv.booking.entity.BookingStatus.CANCELLED
              AND (:excludeBookingId IS NULL OR b.id <> :excludeBookingId)
              AND b.startTime < :endTime
              AND b.endTime > :startTime
            """)
    boolean existsOverlappingBooking(
            @Param("providerId") Long providerId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeBookingId") Long excludeBookingId
    );
}
