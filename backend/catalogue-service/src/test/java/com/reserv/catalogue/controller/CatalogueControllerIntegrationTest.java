package com.reserv.catalogue.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reserv.catalogue.dto.CategoryRequest;
import com.reserv.catalogue.dto.ServiceRequest;
import com.reserv.catalogue.dto.ServiceStatusUpdateRequest;
import com.reserv.catalogue.entity.Category;
import com.reserv.catalogue.entity.Service;
import com.reserv.catalogue.entity.ServiceStatus;
import com.reserv.catalogue.repository.CategoryRepository;
import com.reserv.catalogue.repository.ServiceRepository;
import com.reserv.catalogue.security.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.util.Date;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogueControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private String adminToken;
    private String clientToken;

    @BeforeEach
    void setUp() {
        serviceRepository.deleteAll();
        categoryRepository.deleteAll();

        adminToken = generateToken(1L, "admin@reserv.tn", Role.ADMIN);
        clientToken = generateToken(7L, "client@reserv.tn", Role.CLIENT);
    }

    @Test
    @DisplayName("Securite JWT : operations d'administration refusees sans token (401) ou avec role CLIENT (403)")
    void securityEnforcedOnAdminEndpoints() throws Exception {
        CategoryRequest request = new CategoryRequest("Plomberie", "Travaux de plomberie");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("CRUD complet des categories avec validation, unicite (409) et 404")
    void categoryCrudEndToEnd() throws Exception {
        // Validation error (blank name -> 400)
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequest("   ", "desc"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.name").exists());

        // Create -> 201
        String createdJson = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequest("Coiffure", "Coupe et soins"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Coiffure"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long categoryId = objectMapper.readTree(createdJson).get("id").asLong();

        // Duplicate name -> 409
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequest("coiffure", "Doublon"))))
                .andExpect(status().isConflict());

        // Public GET list & by id -> 200
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/categories/{id}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Coiffure"));

        // Unknown id -> 404
        mockMvc.perform(get("/api/categories/9999"))
                .andExpect(status().isNotFound());

        // Update -> 200
        mockMvc.perform(put("/api/categories/{id}", categoryId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequest("Beaute", "Coiffure et esthetique"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Beaute"));

        // Delete -> 204
        mockMvc.perform(delete("/api/categories/{id}", categoryId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/categories/{id}", categoryId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CRUD complet des prestations, relation categorie, recherche, filtres et desactivation")
    void serviceCrudSearchFilterAndStatusLifecycle() throws Exception {
        Category coiffure = categoryRepository.save(Category.builder()
                .name("Coiffure")
                .description("Coiffure mixte")
                .build());
        Category menage = categoryRepository.save(Category.builder()
                .name("Menage")
                .description("Nettoyage a domicile")
                .build());

        // Unknown category -> 404
        ServiceRequest invalidCatReq = ServiceRequest.builder()
                .name("Coupe")
                .description("Test")
                .price(new BigDecimal("25.000"))
                .duration(45)
                .categoryId(9999L)
                .build();

        mockMvc.perform(post("/api/services")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCatReq)))
                .andExpect(status().isNotFound());

        // Validation errors (negative price, non-positive duration -> 400)
        ServiceRequest invalidDataReq = ServiceRequest.builder()
                .name("")
                .price(new BigDecimal("-5.000"))
                .duration(0)
                .categoryId(coiffure.getId())
                .build();

        mockMvc.perform(post("/api/services")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDataReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.price").exists())
                .andExpect(jsonPath("$.validationErrors.duration").exists());

        // Create service 1 -> 201
        ServiceRequest coupeReq = ServiceRequest.builder()
                .name("Coupe de cheveux")
                .description("Coupe et mise en forme")
                .price(new BigDecimal("25.000"))
                .duration(45)
                .categoryId(coiffure.getId())
                .build();

        String createdJson = mockMvc.perform(post("/api/services")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(coupeReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("Available"))
                .andExpect(jsonPath("$.categoryId").value(coiffure.getId()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long coupeId = objectMapper.readTree(createdJson).get("id").asLong();

        // Create service 2 in another category
        Service menageService = Service.builder()
                .name("Nettoyage complet")
                .description("Appartement 3 pieces")
                .price(new BigDecimal("80.000"))
                .duration(120)
                .status(ServiceStatus.Available)
                .build();
        menageService.assignCategory(menage);
        serviceRepository.save(menageService);

        // Cannot delete category with attached services -> 409
        mockMvc.perform(delete("/api/categories/{id}", coiffure.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict());

        // Filter by category -> 200
        mockMvc.perform(get("/api/services/category/{categoryId}", coiffure.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Coupe de cheveux"));

        // Search by name -> 200
        mockMvc.perform(get("/api/services/search").param("name", "cheveux"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(coupeId));

        // Filter by price -> 200 & invalid bounds -> 400
        mockMvc.perform(get("/api/services")
                        .param("minPrice", "20")
                        .param("maxPrice", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(coupeId));

        mockMvc.perform(get("/api/services")
                        .param("minPrice", "100")
                        .param("maxPrice", "50"))
                .andExpect(status().isBadRequest());

        // Update service -> 200
        ServiceRequest updateReq = ServiceRequest.builder()
                .name("Coupe de cheveux premium")
                .description("Coupe, shampoing et coiffage")
                .price(new BigDecimal("35.000"))
                .duration(60)
                .categoryId(coiffure.getId())
                .build();

        mockMvc.perform(put("/api/services/{id}", coupeId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Coupe de cheveux premium"))
                .andExpect(jsonPath("$.price").value(35.0));

        // Deactivate service -> excluded from normal catalogue consultation, still reachable by ID
        mockMvc.perform(patch("/api/services/{id}/status", coupeId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ServiceStatusUpdateRequest(ServiceStatus.Inavailable))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Inavailable"));

        mockMvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Nettoyage complet"));

        mockMvc.perform(get("/api/services/{id}", coupeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Inavailable"));

        // Reactivate -> 200
        mockMvc.perform(patch("/api/services/{id}/status", coupeId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ServiceStatusUpdateRequest(ServiceStatus.Available))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Available"));

        // Delete -> 204
        mockMvc.perform(delete("/api/services/{id}", coupeId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/services/{id}", coupeId))
                .andExpect(status().isNotFound());
    }

    private String generateToken(Long userId, String email, Role role) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        Date now = new Date();
        return Jwts.builder()
                .subject(email)
                .claim("id", userId)
                .claim("email", email)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + 900_000))
                .signWith(key)
                .compact();
    }
}
