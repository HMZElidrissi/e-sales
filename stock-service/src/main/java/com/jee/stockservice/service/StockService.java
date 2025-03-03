package com.jee.stockservice.service;

import com.jee.stockservice.dto.StockDTO;

import java.util.Optional;

public interface StockService {
    Optional<StockDTO> getStockByProductId(Long productId);
    void updateStock(Long productId, int quantity);
}
