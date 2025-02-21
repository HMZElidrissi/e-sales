package com.jee.stockservice.controller;

import com.jee.stockservice.dto.ProductDTO;
import com.jee.stockservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/product")
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductDTO> addProduct(@RequestBody ProductDTO productDTO) {
        return ResponseEntity.ok(productService.addProduct(productDTO));
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProduct() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        Optional<ProductDTO> product = productService.getProductById(id);
        return product.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        return ResponseEntity.ok(productService.updateProduct(id, productDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
