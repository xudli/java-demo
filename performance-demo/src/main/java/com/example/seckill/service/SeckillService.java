package com.example.seckill.service;

import com.example.seckill.model.SeckillMessage;

public interface SeckillService {
    boolean seckill(Long userId, Long productId);
    void handleSeckillMessage(SeckillMessage message);
} 