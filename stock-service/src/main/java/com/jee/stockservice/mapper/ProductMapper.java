package com.jee.stockservice.mapper;

import com.jee.stockservice.dto.ProductDTO;
import com.jee.stockservice.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "stock.id", target = "stockId")
    ProductDTO toDto(Product product);

    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(source = "stockId", target = "stock.id")
    Product toEntity(ProductDTO productDTO);
}
