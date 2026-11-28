package com.reserv.catalogue.exception;

/**
 * Exception levee lorsqu'une ressource demandee est introuvable (HTTP 404).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
