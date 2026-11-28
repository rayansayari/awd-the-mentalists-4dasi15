package com.reserv.catalogue.exception;

/**
 * Exception levee en cas de conflit de donnees ou d'unicite (HTTP 409).
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
