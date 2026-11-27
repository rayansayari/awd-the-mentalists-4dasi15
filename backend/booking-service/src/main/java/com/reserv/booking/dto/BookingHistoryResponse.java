package com.reserv.booking.dto;

import com.reserv.booking.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO représentant une entrée d'historique de changement de statut.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingHistoryResponse {

    private Long id;
    private Long bookingId;
    private BookingStatus oldStatus;
    private BookingStatus newStatus;
    private LocalDateTime changedAt;
}
