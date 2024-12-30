package com.example.seckill.controller;

import com.example.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seckill")
@Slf4j
public class SeckillController {
    
    @Autowired
    private SeckillService seckillService;
    
    @PostMapping("/{productId}")
    public ResponseEntity<String> seckill(@PathVariable Long productId, @RequestParam Long userId) {
        boolean success = seckillService.seckill(userId, productId);
        if (success) {
            return ResponseEntity.ok("秒杀请求提交成功");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("秒杀失败");
        }
    }
} 