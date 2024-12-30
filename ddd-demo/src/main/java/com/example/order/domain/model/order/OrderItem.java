package com.example.order.domain.model.order;

import com.example.order.domain.model.Money;
import com.example.order.domain.model.product.Product;

import lombok.Getter;

@Getter
public class OrderItem {
    private final Product product;
    private final int quantity;
    private final Money price;

    public OrderItem(Product product, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        this.product = product;
        this.quantity = quantity;
        this.price = product.getPrice();
    }

    public Money getSubtotal() {
        return price.multiply(quantity);
    }
} 