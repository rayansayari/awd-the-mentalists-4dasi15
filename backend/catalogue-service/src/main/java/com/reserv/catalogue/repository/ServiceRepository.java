package com.reserv.catalogue.repository;

import com.reserv.catalogue.entity.Service;
import com.reserv.catalogue.entity.ServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository JPA pour la gestion des prestations du catalogue.
 */
@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    boolean existsByCategoryId(Long categoryId);

    List<Service> findByCategoryIdOrderByIdAsc(Long categoryId);

    List<Service> findByCategoryIdAndStatusOrderByIdAsc(Long categoryId, ServiceStatus status);

    List<Service> findByNameContainingIgnoreCaseOrderByIdAsc(String name);

    List<Service> findByNameContainingIgnoreCaseAndStatusOrderByIdAsc(String name, ServiceStatus status);

    @Query("""
            SELECT s FROM CatalogueService s
            WHERE (:status IS NULL OR s.status = :status)
              AND (:minPrice IS NULL OR s.price >= :minPrice)
              AND (:maxPrice IS NULL OR s.price <= :maxPrice)
            ORDER BY s.id ASC
            """)
    List<Service> findByFilters(
            @Param("status") ServiceStatus status,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );
}
