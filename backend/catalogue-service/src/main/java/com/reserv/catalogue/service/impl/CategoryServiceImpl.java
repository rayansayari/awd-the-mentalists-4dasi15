package com.reserv.catalogue.service.impl;

import com.reserv.catalogue.dto.CategoryRequest;
import com.reserv.catalogue.dto.CategoryResponse;
import com.reserv.catalogue.entity.Category;
import com.reserv.catalogue.exception.ConflictException;
import com.reserv.catalogue.exception.ResourceNotFoundException;
import com.reserv.catalogue.repository.CategoryRepository;
import com.reserv.catalogue.repository.ServiceRepository;
import com.reserv.catalogue.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation de la logique metier des categories.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ServiceRepository serviceRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        String normalizedName = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new ConflictException("Une categorie avec le nom '" + normalizedName + "' existe deja");
        }

        Category category = Category.builder()
                .name(normalizedName)
                .description(trimOrNull(request.getDescription()))
                .build();

        return toResponse(categoryRepository.save(category));
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllByOrderByIdAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        return toResponse(findCategoryOrThrow(id));
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = findCategoryOrThrow(id);
        String normalizedName = request.getName().trim();

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
            throw new ConflictException("Une categorie avec le nom '" + normalizedName + "' existe deja");
        }

        category.setName(normalizedName);
        category.setDescription(trimOrNull(request.getDescription()));

        return toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = findCategoryOrThrow(id);
        if (serviceRepository.existsByCategoryId(id)) {
            throw new ConflictException(
                    "Impossible de supprimer la categorie " + id + " : des prestations y sont encore associees"
            );
        }
        categoryRepository.delete(category);
    }

    private Category findCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categorie introuvable avec l'id : " + id));
    }

    private String trimOrNull(String value) {
        return (value == null) ? null : value.trim();
    }

    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
