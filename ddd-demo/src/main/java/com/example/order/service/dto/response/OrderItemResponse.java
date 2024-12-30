package com.example.order.service.dto.response;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class OrderItemResponse {
    private String productId;
    private String productName;
    private int quantity;
    private BigDecimal price;
    private BigDecimal subtotal;

    public static OrderItemResponse from(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();
        response.setProductId(item.getProduct().getId().getValue());
        response.setProductName(item.getProduct().getName());
        response.setQuantity(item.getQuantity());
        response.setPrice(item.getPrice().getAmount());
        response.setSubtotal(item.getSubtotal().getAmount());
        return response;
    }
} 