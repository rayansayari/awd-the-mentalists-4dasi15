package com.reserv.booking.exception;

/**
 * Exception levée en cas de conflit métier : créneau déjà occupé ou transition de statut incohérente (HTTP 409).
 */
public class BookingConflictException extends RuntimeException {

    public BookingConflictException(String message) {
        super(message);
    }
}
