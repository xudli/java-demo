package com.example.seckill.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class Product {
    private Long id;
    private String name;
    private Integer stock;
    private BigDecimal price;
} 