package com.reserv.review.exception;

/**
 * Exception levée lorsque la requête est invalide (note hors de 1..5, prestataire incohérent...) (HTTP 400).
 */
public class InvalidReviewException extends RuntimeException {

    public InvalidReviewException(String message) {
        super(message);
    }
}
