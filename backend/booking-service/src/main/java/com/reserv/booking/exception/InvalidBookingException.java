package com.reserv.booking.exception;

/**
 * Exception levée lorsque les données d'une réservation violent une règle métier de validation (HTTP 400).
 */
public class InvalidBookingException extends RuntimeException {

    public InvalidBookingException(String message) {
        super(message);
    }
}
