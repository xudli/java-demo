package com.example.order.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.order.common.response.Result;
import com.example.order.service.OrderApplicationService;
import com.example.order.service.dto.request.CreateOrderRequest;
import com.example.order.service.dto.response.OrderResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderApplicationService orderApplicationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return Result.success(orderApplicationService.createOrder(request));
    }

    @PostMapping("/{orderId}/confirm")
    public Result<OrderResponse> confirmOrder(@PathVariable String orderId) {
        return Result.success(orderApplicationService.confirmOrder(orderId));
    }

    @GetMapping("/{orderId}")
    public Result<OrderResponse> getOrder(@PathVariable String orderId) {
        return Result.success(orderApplicationService.getOrder(orderId));
    }
} 