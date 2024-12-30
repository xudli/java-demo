package com.example.seckill.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class RateLimiter {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public boolean tryAcquire(String key) {
        String script = "local current = redis.call('get', KEYS[1]) " +
                       "if current and tonumber(current) > 0 then " +
                       "  redis.call('decr', KEYS[1]) " +
                       "  return 1 " +
                       "end " +
                       "return 0";
        
        return redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), 
            Collections.singletonList(key)) == 1;
    }
    
    public void initializeTokens(String key, int tokens) {
        redisTemplate.opsForValue().set(key, tokens);
    }
} 