package com.example.order.domain.service;

import com.example.order.domain.model.CustomerId;
import com.example.order.domain.model.order.Order;
import com.example.order.domain.model.product.Product;
import com.example.order.domain.repository.ProductRepository;
import com.example.order.service.dto.request.OrderItemRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderDomainService {
    private final ProductRepository productRepository;

    public Order createOrder(CustomerId customerId, List<OrderItemRequest> items) {
        Order order = new Order(customerId);
        
        for (OrderItemRequest item : items) {
            Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
                
            if (!product.hasEnoughStock(item.getQuantity())) {
                throw new IllegalStateException("Insufficient stock for product: " + product.getId());
            }
            
            order.addItem(product, item.getQuantity());
            product.decreaseStock(item.getQuantity());
        }
        
        return order;
    }

    public void confirmOrder(Order order) {
        order.confirm();
    }
} 