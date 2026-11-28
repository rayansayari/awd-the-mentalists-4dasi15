package com.reserv.catalogue.service;

import com.reserv.catalogue.dto.CategoryRequest;
import com.reserv.catalogue.dto.CategoryResponse;
import com.reserv.catalogue.entity.Category;
import com.reserv.catalogue.exception.ConflictException;
import com.reserv.catalogue.exception.ResourceNotFoundException;
import com.reserv.catalogue.repository.CategoryRepository;
import com.reserv.catalogue.repository.ServiceRepository;
import com.reserv.catalogue.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category sampleCategory;

    @BeforeEach
    void setUp() {
        sampleCategory = Category.builder()
                .id(1L)
                .name("Coiffure")
                .description("Soins capillaires et coupes")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Creation d'une categorie valide")
    void createCategorySuccess() {
        CategoryRequest request = new CategoryRequest("  Coiffure  ", "  Soins capillaires  ");
        when(categoryRepository.existsByNameIgnoreCase("Coiffure")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(sampleCategory);

        CategoryResponse response = categoryService.createCategory(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Coiffure", response.getName());
    }

    @Test
    @DisplayName("Refus de creation si le nom de categorie existe deja (409)")
    void createCategoryDuplicateThrowsConflict() {
        CategoryRequest request = new CategoryRequest("Coiffure", "Description");
        when(categoryRepository.existsByNameIgnoreCase("Coiffure")).thenReturn(true);

        assertThrows(ConflictException.class, () -> categoryService.createCategory(request));
    }

    @Test
    @DisplayName("Consultation par ID inexistant leve ResourceNotFoundException (404)")
    void getCategoryByIdNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(99L));
    }

    @Test
    @DisplayName("Liste de toutes les categories")
    void getAllCategoriesSuccess() {
        when(categoryRepository.findAllByOrderByIdAsc()).thenReturn(List.of(sampleCategory));

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertEquals(1, result.size());
        assertEquals("Coiffure", result.get(0).getName());
    }

    @Test
    @DisplayName("Refus de suppression d'une categorie contenant des prestations (409)")
    void deleteCategoryWithServicesThrowsConflict() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));
        when(serviceRepository.existsByCategoryId(1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> categoryService.deleteCategory(1L));
    }

    @Test
    @DisplayName("Suppression reussie d'une categorie sans prestations")
    void deleteCategorySuccess() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));
        when(serviceRepository.existsByCategoryId(1L)).thenReturn(false);

        categoryService.deleteCategory(1L);

        verify(categoryRepository).delete(sampleCategory);
    }
}
