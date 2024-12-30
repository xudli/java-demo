package com.example.order.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.order.infrastructure.persistence.entity.OrderEntity;

public interface JpaOrderRepository extends JpaRepository<OrderEntity, String> {
} 