package com.example.order.service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemRequest {
    @NotNull(message = "Product ID cannot be null")
    private String productId;

    @Min(value = 1, message = "Quantity must be greater than 0")
    private int quantity;
} 