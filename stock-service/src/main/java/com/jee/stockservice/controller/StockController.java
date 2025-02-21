package com.jee.stockservice.controller;

import com.jee.stockservice.dto.StockDTO;
import com.jee.stockservice.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockService stockService;

    @GetMapping("/{id}")
    public ResponseEntity<StockDTO> getStockStatus(@PathVariable Long id) {
        Optional<StockDTO> stock = stockService.getStockByProductId(id);
        return stock.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/update")
    public ResponseEntity<Void> updateStock(@PathVariable Long id, @RequestParam int quantity) {
        stockService.updateStock(id, quantity);
        return ResponseEntity.ok().build();
    }
}
