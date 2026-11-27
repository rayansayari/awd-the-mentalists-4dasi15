package com.awd.monolith.user.service;

import com.awd.monolith.user.dto.AuthResponse;
import com.awd.monolith.user.dto.LoginRequest;
import com.awd.monolith.user.dto.RegisterRequest;
import com.awd.monolith.user.dto.UserResponse;
import com.awd.monolith.user.entity.RefreshToken;
import com.awd.monolith.user.entity.Role;
import com.awd.monolith.user.entity.User;
import com.awd.monolith.user.exception.ConflictException;
import com.awd.monolith.user.exception.ForbiddenException;
import com.awd.monolith.user.exception.ResourceNotFoundException;
import com.awd.monolith.user.exception.UnauthorizedException;
import com.awd.monolith.user.repository.RefreshTokenRepository;
import com.awd.monolith.user.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles user registration, authentication, and refresh token lifecycle.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long refreshExpirationMs;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    @Transactional
    public AuthResponse register(RegisterRequest dto) {
        String normalizedEmail = dto.email().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Email is already registered");
        }

        Role targetRole = dto.role() == null ? Role.CLIENT : dto.role();
        if (targetRole == Role.ADMIN) {
            throw new ForbiddenException("Cannot self-register with ADMIN role");
        }

        User user = new User(
                dto.name().trim(),
                dto.lastname().trim(),
                normalizedEmail,
                passwordEncoder.encode(dto.password()),
                targetRole,
                dto.imageUrl()
        );
        User savedUser = userRepository.save(user);
        return issueTokenPair(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest dto) {
        String normalizedEmail = dto.email().trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(dto.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        return issueTokenPair(user);
    }

    @Transactional
    public AuthResponse rotateRefresh(String rawToken) {
        String tokenHash = hashToken(rawToken);
        RefreshToken existing = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (existing.isRevoked() || existing.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token is expired or revoked");
        }

        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        return issueTokenPair(existing.getUser());
    }

    @Transactional
    public void logout(String rawToken) {
        String tokenHash = hashToken(rawToken);
        RefreshToken existing = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (!existing.isRevoked()) {
            existing.setRevoked(true);
            refreshTokenRepository.save(existing);
        }
    }

    @Transactional
    public void logoutAll(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        refreshTokenRepository.revokeAllByUser(user);
    }

    private AuthResponse issueTokenPair(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String rawRefreshToken = generateRawRefreshToken();
        String tokenHash = hashToken(rawRefreshToken);

        RefreshToken refreshToken = new RefreshToken(
                user,
                tokenHash,
                Instant.now().plusMillis(refreshExpirationMs)
        );
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.of(
                accessToken,
                rawRefreshToken,
                jwtService.getAccessExpirationSeconds(),
                UserResponse.from(user)
        );
    }

    private String generateRawRefreshToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new UnauthorizedException("Refresh token must not be blank");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm not available", ex);
        }
    }
}
