package com.reserv.catalogue.service;

import com.reserv.catalogue.dto.ServiceRequest;
import com.reserv.catalogue.dto.ServiceResponse;
import com.reserv.catalogue.dto.ServiceStatusUpdateRequest;

import java.math.BigDecimal;
import java.util.List;

/**
 * Contrat de gestion et consultation des prestations du catalogue.
 */
public interface CatalogueService {

    ServiceResponse createService(ServiceRequest request);

    List<ServiceResponse> getServices(BigDecimal minPrice, BigDecimal maxPrice, boolean includeUnavailable);

    ServiceResponse getServiceById(Long id);

    List<ServiceResponse> getServicesByCategory(Long categoryId, boolean includeUnavailable);

    List<ServiceResponse> searchServicesByName(String name, boolean includeUnavailable);

    ServiceResponse updateService(Long id, ServiceRequest request);

    ServiceResponse updateServiceStatus(Long id, ServiceStatusUpdateRequest request);

    void deleteService(Long id);
}
