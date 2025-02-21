package com.jee.stockservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private Long categoryId;
    private Long stockId;
}
