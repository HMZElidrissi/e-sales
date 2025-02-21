package com.jee.stockservice.service;

import com.jee.stockservice.dto.CategoryDTO;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    CategoryDTO addCategory(CategoryDTO categoryDTO);
    List<CategoryDTO> getAllCategories();
    Optional<CategoryDTO> getCategoryById(Long id);
}
