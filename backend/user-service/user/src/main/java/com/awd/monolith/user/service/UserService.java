package com.awd.monolith.user.service;

import com.awd.monolith.user.dto.UpdatePasswordRequest;
import com.awd.monolith.user.dto.UpdateUserRequest;
import com.awd.monolith.user.dto.UserResponse;
import com.awd.monolith.user.entity.Role;
import com.awd.monolith.user.entity.User;
import com.awd.monolith.user.exception.ConflictException;
import com.awd.monolith.user.exception.ResourceNotFoundException;
import com.awd.monolith.user.exception.UnauthorizedException;
import com.awd.monolith.user.repository.RefreshTokenRepository;
import com.awd.monolith.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing user CRUD, role changes, password updates, and domain event hooks.
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    public UserService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            ApplicationEventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return UserResponse.from(getUserOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest dto) {
        User user = getUserOrThrow(id);

        if (dto.name() != null && !dto.name().isBlank()) {
            user.setName(dto.name().trim());
        }
        if (dto.lastname() != null && !dto.lastname().isBlank()) {
            user.setLastname(dto.lastname().trim());
        }
        if (dto.email() != null && !dto.email().isBlank()) {
            String normalizedEmail = dto.email().trim().toLowerCase();
            if (!normalizedEmail.equals(user.getEmail()) && userRepository.existsByEmail(normalizedEmail)) {
                throw new ConflictException("Email is already in use");
            }
            user.setEmail(normalizedEmail);
        }
        if (dto.imageUrl() != null) {
            user.setImageUrl(dto.imageUrl());
        }

        User saved = userRepository.save(user);
        publishEvent("USER_UPDATED", saved);
        return UserResponse.from(saved);
    }

    @Transactional
    public void updatePassword(Long id, UpdatePasswordRequest dto) {
        User user = getUserOrThrow(id);
        if (!passwordEncoder.matches(dto.currentPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(dto.newPassword()));
        User saved = userRepository.save(user);
        refreshTokenRepository.revokeAllByUser(saved);
        publishEvent("USER_PASSWORD_UPDATED", saved);
    }

    @Transactional
    public UserResponse changeRole(Long id, Role role) {
        User user = getUserOrThrow(id);
        user.setRole(role);
        User saved = userRepository.save(user);
        publishEvent("USER_ROLE_CHANGED", saved);
        return UserResponse.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        User user = getUserOrThrow(id);
        refreshTokenRepository.deleteAllByUser(user);
        userRepository.delete(user);
        publishEvent("USER_DELETED", user);
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    /**
     * Hook for publishing domain events (e.g. to a message broker) on user/role/delete actions.
     */
    private void publishEvent(String eventType, User user) {
        UserDomainEvent event = new UserDomainEvent(eventType, user.getId(), user.getEmail(), user.getRole(), Instant.now());
        log.info("Domain event published: type={}, userId={}, role={}", event.eventType(), event.userId(), event.role());
        eventPublisher.publishEvent(event);
    }

    public record UserDomainEvent(
            String eventType,
            Long userId,
            String email,
            Role role,
            Instant occurredAt
    ) {
    }
}
