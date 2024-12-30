package com.example.order.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    PRODUCT_NOT_FOUND(404, "Product not found"),
    ORDER_NOT_FOUND(404, "Order not found"),
    INSUFFICIENT_STOCK(400, "Insufficient stock"),
    INVALID_ORDER_STATUS(400, "Invalid order status"),
    SYSTEM_ERROR(500, "System error");

    private final int code;
    private final String message;
} 