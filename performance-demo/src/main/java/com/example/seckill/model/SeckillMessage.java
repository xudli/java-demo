package com.example.seckill.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SeckillMessage {
    private Long userId;
    private Long productId;
} 