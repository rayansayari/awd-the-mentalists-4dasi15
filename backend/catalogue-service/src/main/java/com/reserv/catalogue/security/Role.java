package com.reserv.catalogue.security;

/**
 * Roles utilisateurs reconnus dans les jetons JWT emis par le User Service.
 */
public enum Role {
    CLIENT,
    PROVIDER,
    ADMIN
}
