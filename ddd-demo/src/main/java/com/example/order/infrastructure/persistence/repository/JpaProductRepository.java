package com.example.order.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

import com.example.order.infrastructure.persistence.entity.ProductEntity;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface JpaProductRepository extends JpaRepository<ProductEntity, String> {
    
    @Override
    @Lock(LockModeType.OPTIMISTIC)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    Optional<ProductEntity> findById(String id);
} 