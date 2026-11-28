package com.reserv.review.exception;

/**
 * Exception levée lorsqu'un avis a déjà été publié pour la réservation (HTTP 409).
 */
public class ReviewConflictException extends RuntimeException {

    public ReviewConflictException(String message) {
        super(message);
    }
}
