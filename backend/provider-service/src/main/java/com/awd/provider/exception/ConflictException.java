package com.awd.provider.exception;

/** Conflit (doublon, ressource utilisee...) -> HTTP 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}
