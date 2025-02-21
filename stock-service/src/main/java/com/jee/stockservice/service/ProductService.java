package com.jee.stockservice.service;

import com.jee.stockservice.dto.ProductDTO;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    ProductDTO addProduct(ProductDTO productDTO);
    List<ProductDTO> getAllProducts();
    Optional<ProductDTO> getProductById(Long id);
    ProductDTO updateProduct(Long id , ProductDTO productDTO);
    void deleteProduct(Long id);
}
