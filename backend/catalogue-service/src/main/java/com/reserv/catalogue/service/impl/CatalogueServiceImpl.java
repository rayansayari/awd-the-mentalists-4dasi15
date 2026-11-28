package com.reserv.catalogue.service.impl;

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
import com.reserv.catalogue.service.CatalogueService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implementation de la logique metier des prestations du catalogue.
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogueServiceImpl implements CatalogueService {

    private final ServiceRepository serviceRepository;
    private final CategoryRepository categoryRepository;
    private final CatalogueEventPublisher eventPublisher;

    @Override
    @Transactional
    public ServiceResponse createService(ServiceRequest request) {
        Category category = findCategoryOrThrow(request.getCategoryId());

        Service service = Service.builder()
                .name(request.getName().trim())
                .description(trimOrNull(request.getDescription()))
                .price(request.getPrice())
                .duration(request.getDuration())
                .status(request.getStatus() != null ? request.getStatus() : ServiceStatus.Available)
                .build();
        service.assignCategory(category);

        Service saved = serviceRepository.save(service);
        eventPublisher.publishServiceCreated(saved);
        return toResponse(saved);
    }

    @Override
    public List<ServiceResponse> getServices(BigDecimal minPrice, BigDecimal maxPrice, boolean includeUnavailable) {
        validatePriceRange(minPrice, maxPrice);
        ServiceStatus statusFilter = includeUnavailable ? null : ServiceStatus.Available;
        return mapToResponseList(serviceRepository.findByFilters(statusFilter, minPrice, maxPrice));
    }

    @Override
    public ServiceResponse getServiceById(Long id) {
        return toResponse(findServiceOrThrow(id));
    }

    @Override
    public List<ServiceResponse> getServicesByCategory(Long categoryId, boolean includeUnavailable) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Categorie introuvable avec l'id : " + categoryId);
        }
        List<Service> services = includeUnavailable
                ? serviceRepository.findByCategoryIdOrderByIdAsc(categoryId)
                : serviceRepository.findByCategoryIdAndStatusOrderByIdAsc(categoryId, ServiceStatus.Available);
        return mapToResponseList(services);
    }

    @Override
    public List<ServiceResponse> searchServicesByName(String name, boolean includeUnavailable) {
        String query = (name == null) ? "" : name.trim();
        List<Service> services = includeUnavailable
                ? serviceRepository.findByNameContainingIgnoreCaseOrderByIdAsc(query)
                : serviceRepository.findByNameContainingIgnoreCaseAndStatusOrderByIdAsc(query, ServiceStatus.Available);
        return mapToResponseList(services);
    }

    @Override
    @Transactional
    public ServiceResponse updateService(Long id, ServiceRequest request) {
        Service service = findServiceOrThrow(id);
        Category category = findCategoryOrThrow(request.getCategoryId());

        ServiceStatus previousStatus = service.getStatus();

        service.setName(request.getName().trim());
        service.setDescription(trimOrNull(request.getDescription()));
        service.setPrice(request.getPrice());
        service.setDuration(request.getDuration());
        service.assignCategory(category);
        if (request.getStatus() != null) {
            service.setStatus(request.getStatus());
        }

        Service saved = serviceRepository.save(service);
        if (previousStatus != ServiceStatus.Inavailable && saved.getStatus() == ServiceStatus.Inavailable) {
            eventPublisher.publishServiceDesactivated(saved);
        } else {
            eventPublisher.publishServiceUpdated(saved);
        }
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ServiceResponse updateServiceStatus(Long id, ServiceStatusUpdateRequest request) {
        Service service = findServiceOrThrow(id);
        service.setStatus(request.getStatus());

        Service saved = serviceRepository.save(service);
        if (saved.getStatus() == ServiceStatus.Inavailable) {
            eventPublisher.publishServiceDesactivated(saved);
        } else {
            eventPublisher.publishServiceUpdated(saved);
        }
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteService(Long id) {
        Service service = findServiceOrThrow(id);
        serviceRepository.delete(service);
        eventPublisher.publishServiceDeleted(id);
    }

    private Category findCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categorie introuvable avec l'id : " + categoryId));
    }

    private Service findServiceOrThrow(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prestation introuvable avec l'id : " + id));
    }

    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Le prix minimum doit etre positif ou nul");
        }
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Le prix maximum doit etre positif ou nul");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BadRequestException("Le prix minimum ne peut pas depasser le prix maximum");
        }
    }

    private String trimOrNull(String value) {
        return (value == null) ? null : value.trim();
    }

    private List<ServiceResponse> mapToResponseList(List<Service> services) {
        return services.stream()
                .map(this::toResponse)
                .toList();
    }

    private ServiceResponse toResponse(Service service) {
        Long resolvedCategoryId = service.getCategoryId() != null
                ? service.getCategoryId()
                : (service.getCategory() != null ? service.getCategory().getId() : null);

        return ServiceResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .description(service.getDescription())
                .price(service.getPrice())
                .duration(service.getDuration())
                .categoryId(resolvedCategoryId)
                .status(service.getStatus())
                .createdAt(service.getCreatedAt())
                .build();
    }
}
