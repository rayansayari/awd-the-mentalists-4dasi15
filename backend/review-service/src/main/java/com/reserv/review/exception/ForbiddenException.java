package com.reserv.review.exception;

/**
 * Exception levée lorsque l'accès est interdit : réservation non terminée, non possédée, rôle insuffisant (HTTP 403).
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
