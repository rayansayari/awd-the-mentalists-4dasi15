package com.awd.monolith.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.awd.monolith.user.dto.ChangeRoleRequest;
import com.awd.monolith.user.dto.LoginRequest;
import com.awd.monolith.user.dto.UpdatePasswordRequest;
import com.awd.monolith.user.dto.UpdateUserRequest;
import com.awd.monolith.user.entity.Role;
import com.awd.monolith.user.entity.User;
import com.awd.monolith.user.repository.RefreshTokenRepository;
import com.awd.monolith.user.repository.UserRepository;
import com.awd.monolith.user.security.RateLimiterFilter;
import com.awd.monolith.user.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RateLimiterFilter rateLimiterFilter;

    private User clientUser;
    private User otherUser;
    private User adminUser;
    private String clientToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        rateLimiterFilter.reset();

        clientUser = userRepository.save(new User(
                "Client", "One", "client1@example.com", passwordEncoder.encode("Password123!"), Role.CLIENT, null));
        otherUser = userRepository.save(new User(
                "Client", "Two", "client2@example.com", passwordEncoder.encode("Password123!"), Role.CLIENT, null));
        adminUser = userRepository.save(new User(
                "Super", "Admin", "admin@example.com", passwordEncoder.encode("Password123!"), Role.ADMIN, null));

        clientToken = jwtService.generateAccessToken(clientUser);
        adminToken = jwtService.generateAccessToken(adminUser);
    }

    @Test
    void getMeAndUpdateMeWorkForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("client1@example.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        UpdateUserRequest update = new UpdateUserRequest("UpdatedName", "UpdatedLast", null, "https://img.test/a.png");
        mockMvc.perform(put("/api/v1/users/me")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("UpdatedName"))
                .andExpect(jsonPath("$.lastname").value("UpdatedLast"))
                .andExpect(jsonPath("$.imageUrl").value("https://img.test/a.png"));
    }

    @Test
    void rbacEnforcedOnGetAllChangeRoleAndDelete() throws Exception {
        // Non-admin forbidden
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/v1/users/{id}/role", otherUser.getId())
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangeRoleRequest(Role.PROVIDER))))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/users/{id}", otherUser.getId())
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());

        // Admin allowed
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        mockMvc.perform(patch("/api/v1/users/{id}/role", otherUser.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangeRoleRequest(Role.PROVIDER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("PROVIDER"));

        mockMvc.perform(delete("/api/v1/users/{id}", otherUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/users/{id}", otherUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void ownershipAndPasswordUpdateEnforced() throws Exception {
        // Client cannot read or update another user
        mockMvc.perform(get("/api/v1/users/{id}", otherUser.getId())
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/users/{id}", otherUser.getId())
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserRequest("Hack", null, null, null))))
                .andExpect(status().isForbidden());

        // Client can read own profile by id
        mockMvc.perform(get("/api/v1/users/{id}", clientUser.getId())
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clientUser.getId()));

        // Password update: wrong current password -> 401
        mockMvc.perform(put("/api/v1/users/{id}/password", clientUser.getId())
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdatePasswordRequest("WrongPass!", "NewStrongPass1!"))))
                .andExpect(status().isUnauthorized());

        // Password update: valid -> 204 and can login with new password
        mockMvc.perform(put("/api/v1/users/{id}/password", clientUser.getId())
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdatePasswordRequest("Password123!", "NewStrongPass1!"))))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("client1@example.com", "NewStrongPass1!"))))
                .andExpect(status().isOk());
    }
}
