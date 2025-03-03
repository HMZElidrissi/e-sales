package com.jee.stockservice.mapper;

import com.jee.stockservice.dto.CategoryDTO;
import com.jee.stockservice.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    CategoryDTO toDto(Category category);
    Category toEntity(CategoryDTO categoryDTO);
}
