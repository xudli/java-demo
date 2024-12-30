package com.example.order.service.dto.response;

import com.example.order.domain.model.order.Order;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private String orderId;
    private String customerId;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;

    public static OrderResponse from(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getId().getValue());
        response.setCustomerId(order.getCustomerId().getValue());
        response.setStatus(order.getStatus().name());
        response.setTotalAmount(order.getTotalAmount().getAmount());
        response.setCreatedAt(order.getCreatedAt());
        response.setItems(order.getItems().stream()
            .map(OrderItemResponse::from)
            .toList());
        return response;
    }
} 