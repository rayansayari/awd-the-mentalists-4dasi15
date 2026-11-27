package com.awd.monolith.user.controller;

import com.awd.monolith.user.dto.ChangeRoleRequest;
import com.awd.monolith.user.dto.UpdatePasswordRequest;
import com.awd.monolith.user.dto.UpdateUserRequest;
import com.awd.monolith.user.dto.UserResponse;
import com.awd.monolith.user.entity.Role;
import com.awd.monolith.user.exception.ForbiddenException;
import com.awd.monolith.user.exception.UnauthorizedException;
import com.awd.monolith.user.security.UserPrincipal;
import com.awd.monolith.user.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(Authentication authentication) {
        UserPrincipal principal = requirePrincipal(authentication);
        return ResponseEntity.ok(userService.findById(principal.id()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMe(
            Authentication authentication,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        UserPrincipal principal = requirePrincipal(authentication);
        return ResponseEntity.ok(userService.update(principal.id(), request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        assertAdminOrSelf(id, authentication);
        return ResponseEntity.ok(userService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication
    ) {
        assertAdminOrSelf(id, authentication);
        return ResponseEntity.ok(userService.update(id, request));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePasswordRequest request,
            Authentication authentication
    ) {
        UserPrincipal principal = requirePrincipal(authentication);
        if (!principal.id().equals(id)) {
            throw new ForbiddenException("You can only update your own password");
        }
        userService.updatePassword(id, request);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/{id}/role", method = {RequestMethod.PATCH, RequestMethod.PUT})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> changeRole(
            @PathVariable Long id,
            @Valid @RequestBody ChangeRoleRequest request
    ) {
        return ResponseEntity.ok(userService.changeRole(id, request.role()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private UserPrincipal requirePrincipal(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal;
        }
        throw new UnauthorizedException("Authentication required");
    }

    private void assertAdminOrSelf(Long targetUserId, Authentication authentication) {
        UserPrincipal principal = requirePrincipal(authentication);
        boolean isAdmin = principal.role() == Role.ADMIN;
        if (!isAdmin && !principal.id().equals(targetUserId)) {
            throw new ForbiddenException("Access denied");
        }
    }
}
