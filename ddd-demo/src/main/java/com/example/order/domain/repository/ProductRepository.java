package com.example.order.domain.repository;

import com.example.order.domain.model.product.Product;
import com.example.order.domain.model.product.ProductId;
import java.util.Optional;

public interface ProductRepository {
    Optional<Product> findById(String productId);
    Product save(Product product);
} 