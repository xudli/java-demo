package com.example.order.domain.model.product;

import com.example.order.domain.model.Money;

import lombok.Getter;

@Getter
public class Product {
    private final ProductId id;
    private final String name;
    private final Money price;
    private int stock;

    public Product(ProductId id, String name, Money price, int stock) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }

        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public boolean hasEnoughStock(int quantity) {
        return stock >= quantity;
    }

    public void decreaseStock(int quantity) {
        if (quantity > stock) {
            throw new IllegalStateException("Insufficient stock");
        }
        stock -= quantity;
    }
} 