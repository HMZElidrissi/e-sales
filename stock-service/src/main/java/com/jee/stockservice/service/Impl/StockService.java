package com.jee.stockservice.service.Impl;

import com.jee.stockservice.dto.StockDTO;
import com.jee.stockservice.entity.Stock;
import com.jee.stockservice.mapper.StockMapper;
import com.jee.stockservice.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StockService implements com.jee.stockservice.service.StockService {

    private final StockRepository stockRepository;
    private final StockMapper stockMapper;

    @Override
    public Optional<StockDTO> getStockByProductId(Long productId) {
        return stockRepository.findByProductId(productId)
                .map(stockMapper::toDto);
    }

    @Override
    public void updateStock(Long productId, int quantity) {
        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Stock not found"));
        stock.setQuantity(quantity);
        stockRepository.save(stock);
    }
}
