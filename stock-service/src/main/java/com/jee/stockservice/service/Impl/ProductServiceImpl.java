package com.jee.stockservice.service.Impl;

import com.jee.stockservice.dto.ProductDTO;
import com.jee.stockservice.entity.Category;
import com.jee.stockservice.entity.Product;
import com.jee.stockservice.entity.Stock;
import com.jee.stockservice.mapper.ProductMapper;
import com.jee.stockservice.repository.CategoryRepository;
import com.jee.stockservice.repository.ProductRepository;
import com.jee.stockservice.repository.StockRepository;
import com.jee.stockservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StockRepository stockRepository;
    private final ProductMapper productMapper;


    @Override
    public ProductDTO addProduct(ProductDTO productDTO) {
        Product product = productMapper.toEntity(productDTO);

        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        product.setCategory(category);

        Stock stock = stockRepository.findById(productDTO.getStockId())
                .orElseThrow(() -> new RuntimeException("Stock not found"));
        product.setStock(stock);

        return productMapper.toDto(productRepository.save(product));
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ProductDTO> getProductById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toDto);
    }

    @Override
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    existingProduct.setName(productDTO.getName());
                    existingProduct.setPrice(productDTO.getPrice());

                    Category category = categoryRepository.findById(productDTO.getCategoryId())
                            .orElseThrow(() -> new RuntimeException("Category not found"));
                    existingProduct.setCategory(category);

                    return productMapper.toDto(productRepository.save(existingProduct));
                })
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
