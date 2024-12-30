package com.example.seckill.service.impl;

import com.example.seckill.model.Order;
import com.example.seckill.model.SeckillMessage;
import com.example.seckill.service.SeckillService;
import com.example.seckill.config.RabbitMQConfig;
import com.example.seckill.util.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class SeckillServiceImpl implements SeckillService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private RateLimiter rateLimiter;
    
    @Override
    public boolean seckill(Long userId, Long productId) {
        // 限流检查
        if (!rateLimiter.tryAcquire("seckill:" + productId)) {
            log.info("秒杀被限流，用户：{}，商品：{}", userId, productId);
            return false;
        }
        
        // 检查库存
        String stockKey = "stock:" + productId;
        Integer stock = (Integer) redisTemplate.opsForValue().get(stockKey);
        if (stock == null || stock <= 0) {
            log.info("库存不足，用户：{}，商品：{}", userId, productId);
            return false;
        }
        
        // 检查是否重复下单
        String orderKey = "order:" + userId + ":" + productId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(orderKey))) {
            log.info("重复下单，用户：{}，商品：{}", userId, productId);
            return false;
        }
        
        // 预减库存
        Long remainStock = redisTemplate.opsForValue().decrement(stockKey);
        if (remainStock < 0) {
            redisTemplate.opsForValue().increment(stockKey);
            return false;
        }
        
        // 发送消息到队列
        SeckillMessage message = new SeckillMessage(userId, productId);
        rabbitTemplate.convertAndSend(RabbitMQConfig.SECKILL_QUEUE, message);
        
        return true;
    }
    
    @RabbitListener(queues = RabbitMQConfig.SECKILL_QUEUE)
    public void handleSeckillMessage(SeckillMessage message) {
        try {
            // 创建订单
            Order order = createOrder(message.getUserId(), message.getProductId());
            // 记录订单信息到Redis
            String orderKey = "order:" + message.getUserId() + ":" + message.getProductId();
            redisTemplate.opsForValue().set(orderKey, order);
            log.info("秒杀成功，用户：{}，商品：{}", message.getUserId(), message.getProductId());
        } catch (Exception e) {
            log.error("处理秒杀消息异常", e);
            // 恢复库存
            String stockKey = "stock:" + message.getProductId();
            redisTemplate.opsForValue().increment(stockKey);
        }
    }
    
    private Order createOrder(Long userId, Long productId) {
        // 实际项目中这里需要调用订单服务创建订单
        return new Order(
            generateOrderId(),
            userId,
            productId,
            BigDecimal.valueOf(999),
            new Date()
        );
    }
    
    private Long generateOrderId() {
        // 简单的订单ID生成方法，实际项目中应该使用更复杂的算法
        return System.currentTimeMillis() + ThreadLocalRandom.current().nextLong(1000);
    }
} 