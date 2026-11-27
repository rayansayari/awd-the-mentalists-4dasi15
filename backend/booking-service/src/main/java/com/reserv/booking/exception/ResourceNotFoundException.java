package com.reserv.booking.exception;

/**
 * Exception levée lorsqu'une ressource demandée (ex. réservation) est introuvable (HTTP 404).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
