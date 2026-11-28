package com.reserv.catalogue.service;

import com.reserv.catalogue.dto.ServiceRequest;
import com.reserv.catalogue.dto.ServiceResponse;
import com.reserv.catalogue.dto.ServiceStatusUpdateRequest;
import com.reserv.catalogue.entity.Category;
import com.reserv.catalogue.entity.Service;
import com.reserv.catalogue.entity.ServiceStatus;
import com.reserv.catalogue.event.CatalogueEventPublisher;
import com.reserv.catalogue.exception.BadRequestException;
import com.reserv.catalogue.exception.ResourceNotFoundException;
import com.reserv.catalogue.repository.CategoryRepository;
import com.reserv.catalogue.repository.ServiceRepository;
import com.reserv.catalogue.service.impl.CatalogueServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogueServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CatalogueEventPublisher eventPublisher;

    @InjectMocks
    private CatalogueServiceImpl catalogueService;

    private Category category;
    private Service service;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(2L)
                .name("Coiffure")
                .description("Prestations coiffure")
                .createdAt(LocalDateTime.now())
                .build();

        service = Service.builder()
                .id(3L)
                .name("Coupe de cheveux")
                .description("Coupe et mise en forme")
                .price(new BigDecimal("25.000"))
                .duration(45)
                .status(ServiceStatus.Available)
                .categoryId(2L)
                .category(category)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Creation d'une prestation valide avec publication ServiceCreated")
    void createServiceSuccess() {
        ServiceRequest request = ServiceRequest.builder()
                .name("Coupe de cheveux")
                .description("Coupe et mise en forme")
                .price(new BigDecimal("25.000"))
                .duration(45)
                .categoryId(2L)
                .build();

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(serviceRepository.save(any(Service.class))).thenReturn(service);

        ServiceResponse response = catalogueService.createService(request);

        assertNotNull(response);
        assertEquals(3L, response.getId());
        assertEquals(ServiceStatus.Available, response.getStatus());
        assertEquals(2L, response.getCategoryId());
        verify(eventPublisher).publishServiceCreated(service);
    }

    @Test
    @DisplayName("Refus de creation si la categorie n'existe pas (404)")
    void createServiceMissingCategoryThrowsNotFound() {
        ServiceRequest request = ServiceRequest.builder()
                .name("Coupe")
                .price(new BigDecimal("20.000"))
                .duration(30)
                .categoryId(999L)
                .build();

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> catalogueService.createService(request));
    }

    @Test
    @DisplayName("Desactivation d'une prestation publie ServiceDesactivated")
    void deactivateServicePublishesEvent() {
        when(serviceRepository.findById(3L)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(Service.class))).thenAnswer(inv -> inv.getArgument(0));

        ServiceResponse response = catalogueService.updateServiceStatus(
                3L,
                new ServiceStatusUpdateRequest(ServiceStatus.Inavailable)
        );

        assertEquals(ServiceStatus.Inavailable, response.getStatus());
        verify(eventPublisher).publishServiceDesactivated(service);
    }

    @Test
    @DisplayName("Suppression d'une prestation publie ServiceDeleted")
    void deleteServicePublishesEvent() {
        when(serviceRepository.findById(3L)).thenReturn(Optional.of(service));

        catalogueService.deleteService(3L);

        verify(serviceRepository).delete(service);
        verify(eventPublisher).publishServiceDeleted(3L);
    }

    @Test
    @DisplayName("Refus de filtre si minPrice > maxPrice (400)")
    void getServicesInvalidPriceRangeThrowsBadRequest() {
        assertThrows(
                BadRequestException.class,
                () -> catalogueService.getServices(new BigDecimal("50"), new BigDecimal("10"), false)
        );
    }

    @Test
    @DisplayName("Filtrage par plage de prix valide")
    void getServicesValidPriceRange() {
        when(serviceRepository.findByFilters(ServiceStatus.Available, new BigDecimal("10"), new BigDecimal("30")))
                .thenReturn(List.of(service));

        List<ServiceResponse> result = catalogueService.getServices(
                new BigDecimal("10"), new BigDecimal("30"), false
        );

        assertEquals(1, result.size());
        assertEquals("Coupe de cheveux", result.get(0).getName());
    }
}
