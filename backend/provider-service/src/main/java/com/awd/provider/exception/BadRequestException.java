package com.awd.provider.exception;

/** Requete invalide (regle metier) -> HTTP 400. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}
