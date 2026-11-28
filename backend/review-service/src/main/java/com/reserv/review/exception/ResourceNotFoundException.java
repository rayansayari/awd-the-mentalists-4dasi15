package com.reserv.review.exception;

/**
 * Exception levée lorsqu'une ressource demandée (avis, note moyenne...) est introuvable (HTTP 404).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
