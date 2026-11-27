package com.awd.monolith.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.awd.monolith.user.entity.Role;
import com.awd.monolith.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tc2lnbmluZy1hdC1sZWFzdC0zMi1ieXRlcw==";

    @Test
    void generateAndExtractClaimsIncludesIdEmailAndRole() {
        JwtService jwtService = new JwtService(SECRET, 900_000L);
        User user = new User("Alice", "Smith", "alice@example.com", "hashed", Role.PROVIDER, null);
        user.setId(42L);

        String token = jwtService.generateAccessToken(user);
        Claims claims = jwtService.extractClaims(token);

        assertThat(jwtService.extractUserId(claims)).isEqualTo(42L);
        assertThat(jwtService.extractEmail(claims)).isEqualTo("alice@example.com");
        assertThat(jwtService.extractRole(claims)).isEqualTo(Role.PROVIDER);
    }

    @Test
    void extractClaimsRejectsTamperedToken() {
        JwtService jwtService = new JwtService(SECRET, 900_000L);
        User user = new User("Alice", "Smith", "alice@example.com", "hashed", Role.CLIENT, null);
        user.setId(1L);

        String token = jwtService.generateAccessToken(user);
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThatThrownBy(() -> jwtService.extractClaims(tampered))
                .isInstanceOf(JwtException.class);
    }
}
