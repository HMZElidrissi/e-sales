package com.jee.stockservice.mapper;

import com.jee.stockservice.dto.StockDTO;
import com.jee.stockservice.entity.Stock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface StockMapper {
    StockMapper INSTANCE = Mappers.getMapper(StockMapper.class);

    @Mapping(source = "product.id", target = "productId")
    StockDTO toDto(Stock stock);

    @Mapping(source = "productId", target = "product.id")
    Stock toEntity(StockDTO stockDTO);
}
