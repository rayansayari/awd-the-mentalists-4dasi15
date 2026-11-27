package com.awd.monolith.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.awd.monolith.user.dto.LoginRequest;
import com.awd.monolith.user.dto.RefreshTokenRequest;
import com.awd.monolith.user.dto.RegisterRequest;
import com.awd.monolith.user.entity.RefreshToken;
import com.awd.monolith.user.entity.Role;
import com.awd.monolith.user.entity.User;
import com.awd.monolith.user.repository.RefreshTokenRepository;
import com.awd.monolith.user.repository.UserRepository;
import com.awd.monolith.user.security.RateLimiterFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RateLimiterFilter rateLimiterFilter;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        rateLimiterFilter.reset();
    }

    @Test
    void registerLoginRefreshLogoutAndLogoutAllFlow() throws Exception {
        RegisterRequest register = new RegisterRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                "StrongPass123!",
                Role.CLIENT,
                "https://cdn.example.com/avatar.png"
        );

        MvcResult regResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.user.role").value("CLIENT"))
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist())
                .andReturn();

        JsonNode regJson = objectMapper.readTree(regResult.getResponse().getContentAsString());
        String firstRefresh = regJson.get("refreshToken").asText();

        // Verify password and refresh token are stored hashed
        User savedUser = userRepository.findByEmail("john.doe@example.com").orElseThrow();
        assertThat(savedUser.getPasswordHash()).isNotEqualTo("StrongPass123!");
        List<RefreshToken> tokens = refreshTokenRepository.findAll();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).getTokenHash()).isNotEqualTo(firstRefresh);

        // Login
        LoginRequest login = new LoginRequest("john.doe@example.com", "StrongPass123!");
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String accessToken = loginJson.get("accessToken").asText();
        String secondRefresh = loginJson.get("refreshToken").asText();

        // Rotate refresh token
        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest(secondRefresh))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        String rotatedRefresh = objectMapper.readTree(refreshResult.getResponse().getContentAsString())
                .get("refreshToken").asText();
        assertThat(rotatedRefresh).isNotEqualTo(secondRefresh);

        // Reusing old refresh token fails with 401
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest(secondRefresh))))
                .andExpect(status().isUnauthorized());

        // Logout revokes rotatedRefresh
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest(rotatedRefresh))))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest(rotatedRefresh))))
                .andExpect(status().isUnauthorized());

        // Logout-all revokes remaining active tokens (like firstRefresh)
        mockMvc.perform(post("/api/v1/auth/logout-all")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest(firstRefresh))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerDuplicateEmailReturns409AndAdminSelfRegisterReturns403() throws Exception {
        RegisterRequest first = new RegisterRequest("Jane", "Doe", "jane@example.com", "StrongPass123!", null, null);
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isConflict());

        RegisterRequest adminAttempt = new RegisterRequest("Boss", "Admin", "boss@example.com", "StrongPass123!", Role.ADMIN, null);
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminAttempt)))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginWithWrongPasswordReturns401AndInvalidInputReturns400() throws Exception {
        RegisterRequest register = new RegisterRequest("Bob", "Lee", "bob@example.com", "StrongPass123!", null, null);
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("bob@example.com", "WrongPass!"))))
                .andExpect(status().isUnauthorized());

        RegisterRequest invalid = new RegisterRequest("", "", "not-an-email", "123", null, null);
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }
}
