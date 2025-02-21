package com.jee.stockservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockDTO {
    private Long id;
    private Long productId;
    private Integer quantity;
}
