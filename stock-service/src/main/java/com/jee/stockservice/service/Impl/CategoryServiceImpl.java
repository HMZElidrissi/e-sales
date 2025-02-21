package com.jee.stockservice.service.Impl;

import com.jee.stockservice.dto.CategoryDTO;
import com.jee.stockservice.entity.Category;
import com.jee.stockservice.mapper.CategoryMapper;
import com.jee.stockservice.mapper.StockMapper;
import com.jee.stockservice.repository.CategoryRepository;
import com.jee.stockservice.repository.StockRepository;
import com.jee.stockservice.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryDTO addCategory(CategoryDTO categoryDTO) {
        Category category = categoryMapper.toEntity(categoryDTO);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CategoryDTO> getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::toDto);
    }
}
