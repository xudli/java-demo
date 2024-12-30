package com.example.order.domain.repository;

import com.example.order.domain.model.order.Order;
import com.example.order.domain.model.order.OrderId;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(OrderId orderId);
} 