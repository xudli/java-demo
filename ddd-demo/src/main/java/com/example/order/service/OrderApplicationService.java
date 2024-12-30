package com.example.order.service;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.order.common.exception.BusinessException;
import com.example.order.domain.model.CustomerId;
import com.example.order.domain.model.order.Order;
import com.example.order.domain.model.order.OrderId;
import com.example.order.domain.model.product.ProductId;
import com.example.order.domain.repository.OrderRepository;
import com.example.order.domain.repository.ProductRepository;
import com.example.order.service.dto.request.CreateOrderRequest;
import com.example.order.service.dto.request.OrderItemRequest;
import com.example.order.service.dto.response.OrderResponse;

import lombok.RequiredArgsConstructor;
import lombok.var;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderApplicationService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        CustomerId customerId = new CustomerId(request.getCustomerId());
        Order order = new Order(customerId);

        request.getItems().forEach(item -> {
            ProductId productId = new ProductId(item.getProductId());
            var product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("Product not found: " + productId.getValue()));
            
            order.addItem(product, item.getQuantity());
        });

        Order savedOrder = orderRepository.save(order);
        return OrderResponse.from(savedOrder);
    }

    @Transactional
    public OrderResponse confirmOrder(String orderId) {
        Order order = orderRepository.findById(new OrderId(orderId))
            .orElseThrow(() -> new BusinessException("Order not found: " + orderId));
            
        order.confirm();
        Order savedOrder = orderRepository.save(order);
        return OrderResponse.from(savedOrder);
    }

    public OrderResponse getOrder(String orderId) {
        Order order = orderRepository.findById(new OrderId(orderId))
            .orElseThrow(() -> new BusinessException("Order not found: " + orderId));
        return OrderResponse.from(order);
    }
} 