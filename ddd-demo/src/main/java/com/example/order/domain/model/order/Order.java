package com.example.order.domain.model.order;

import com.example.order.domain.model.CustomerId;
import com.example.order.domain.model.Money;
import com.example.order.domain.model.product.Product;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class Order {
    private final OrderId id;
    private final CustomerId customerId;
    private OrderStatus status;
    private Money totalAmount;
    private final LocalDateTime createdAt;
    private final List<OrderItem> items;

    public Order(CustomerId customerId) {
        this.id = new OrderId();
        this.customerId = customerId;
        this.status = OrderStatus.PENDING;
        this.totalAmount = new Money(BigDecimal.ZERO);
        this.createdAt = LocalDateTime.now();
        this.items = new ArrayList<>();
    }

    public void addItem(Product product, int quantity) {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot modify confirmed order");
        }
        
        OrderItem item = new OrderItem(product, quantity);
        items.add(item);
        recalculateTotal();
    }

    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Order is not pending");
        }
        if (items.isEmpty()) {
            throw new IllegalStateException("Order has no items");
        }
        status = OrderStatus.CONFIRMED;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    private void recalculateTotal() {
        this.totalAmount = items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(new Money(BigDecimal.ZERO), Money::add);
    }
} 