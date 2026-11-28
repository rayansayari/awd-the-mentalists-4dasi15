package com.reserv.review.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Réservation terminée (événement booking.completed) pouvant faire l'objet d'un avis.
 * Une ReviewableBooking autorise au plus un Review.
 */
@Entity
@Table(name = "reviewable_bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ReviewableBooking {

    @Id
    @Column(name = "booking_id")
    @EqualsAndHashCode.Include
    private Long bookingId;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @Column(name = "reviewed", nullable = false)
    private Boolean reviewed;

    /**
     * Marque la réservation comme déjà évaluée.
     */
    public void markAsReviewed() {
        this.reviewed = Boolean.TRUE;
    }
}
