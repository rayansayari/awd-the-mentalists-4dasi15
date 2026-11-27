package com.awd.monolith.user.security;

import com.awd.monolith.user.entity.Role;

/**
 * Authenticated user identity stored as the Spring Security principal.
 */
public record UserPrincipal(
        Long id,
        String email,
        Role role
) {
}
