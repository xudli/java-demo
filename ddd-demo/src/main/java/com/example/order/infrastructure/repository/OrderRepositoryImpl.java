package com.example.order.infrastructure.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.order.domain.model.order.Order;
import com.example.order.domain.model.order.OrderId;
import com.example.order.domain.repository.OrderRepository;
import com.example.order.infrastructure.persistence.mapper.OrderMapper;
import com.example.order.infrastructure.persistence.repository.JpaOrderRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {
    private final JpaOrderRepository jpaOrderRepository;
    private final OrderMapper orderMapper;

    @Override
    public Order save(Order order) {
        var entity = orderMapper.toEntity(order);
        var savedEntity = jpaOrderRepository.save(entity);
        return orderMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        return jpaOrderRepository.findById(orderId.getValue())
            .map(orderMapper::toDomain);
    }
} 