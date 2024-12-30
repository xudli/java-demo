package com.example.order.infrastructure.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.order.domain.model.product.Product;
import com.example.order.domain.model.product.ProductId;
import com.example.order.domain.repository.ProductRepository;
import com.example.order.infrastructure.persistence.mapper.ProductMapper;
import com.example.order.infrastructure.persistence.repository.JpaProductRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {
    private final JpaProductRepository jpaProductRepository;
    private final ProductMapper productMapper;

    @Override
    public Optional<Product> findById(ProductId productId) {
        return jpaProductRepository.findById(productId.getValue())
            .map(productMapper::toDomain);
    }

    @Override
    public Product save(Product product) {
        var entity = productMapper.toEntity(product);
        var savedEntity = jpaProductRepository.save(entity);
        return productMapper.toDomain(savedEntity);
    }
} 