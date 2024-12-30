package com.example.seckill.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
public class Order {
    private Long id;
    private Long userId;
    private Long productId;
    private BigDecimal price;
    private Date createTime;
} 