package com.reserv.catalogue.service;

import com.reserv.catalogue.dto.CategoryRequest;
import com.reserv.catalogue.dto.CategoryResponse;

import java.util.List;

/**
 * Contrat de gestion des categories du catalogue.
 */
public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest request);

    List<CategoryResponse> getAllCategories();

    CategoryResponse getCategoryById(Long id);

    CategoryResponse updateCategory(Long id, CategoryRequest request);

    void deleteCategory(Long id);
}
