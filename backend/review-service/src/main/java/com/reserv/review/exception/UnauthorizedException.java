package com.reserv.review.exception;

/**
 * Exception levée lorsque l'authentification est requise mais absente (HTTP 401).
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
