package com.reserv.catalogue.exception;

/**
 * Exception levee lorsque les donnees ou parametres de la requete sont invalides (HTTP 400).
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
