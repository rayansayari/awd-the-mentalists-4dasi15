package com.reserv.catalogue.security;

/**
 * Identite authentifiee extraite du JWT et stockee dans le SecurityContext.
 */
public record UserPrincipal(
        Long id,
        String email,
        Role role
) {
}
