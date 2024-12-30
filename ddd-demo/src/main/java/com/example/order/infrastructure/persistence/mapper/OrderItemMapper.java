package com.example.order.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.order.domain.model.Money;
import com.example.order.domain.model.order.OrderItem;
import com.example.order.infrastructure.persistence.entity.OrderItemEntity;

@Mapper(componentModel = "spring", 
        uses = {ProductMapper.class},
        imports = {Money.class})
public interface OrderItemMapper {
    
    @Mapping(target = "productId", source = "product.id.value")
    @Mapping(target = "price", source = "price.amount")
    OrderItemEntity toEntity(OrderItem orderItem);

    @Mapping(target = "product", expression = "java(productMapper.toDomain(entity))")
    @Mapping(target = "price", expression = "java(new Money(entity.getPrice()))")
    OrderItem toDomain(OrderItemEntity entity);
} 