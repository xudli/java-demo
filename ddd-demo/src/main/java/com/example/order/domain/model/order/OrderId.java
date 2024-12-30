package com.example.order.domain.model.order;

import java.util.UUID;

import lombok.Getter;

@Getter
public class OrderId {
    private final String value;

    public OrderId() {
        this.value = UUID.randomUUID().toString();
    }

    public OrderId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be empty");
        }
        this.value = value;
    }
} 