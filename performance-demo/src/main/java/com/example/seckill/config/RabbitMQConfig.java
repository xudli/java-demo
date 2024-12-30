package com.example.seckill.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String SECKILL_QUEUE = "seckill.queue";

    @Bean
    public Queue seckillQueue() {
        return new Queue(SECKILL_QUEUE, true);
    }
} 