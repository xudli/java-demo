package com.example.order.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.order.domain.model.CustomerId;
import com.example.order.domain.model.Money;
import com.example.order.domain.model.order.Order;
import com.example.order.domain.model.order.OrderId;
import com.example.order.infrastructure.persistence.entity.OrderEntity;

@Mapper(componentModel = "spring", 
        uses = {OrderItemMapper.class},
        imports = {OrderId.class, CustomerId.class, Money.class})
public interface OrderMapper {
    
    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "customerId", source = "customerId.value")
    @Mapping(target = "totalAmount", source = "totalAmount.amount")
    OrderEntity toEntity(Order order);

    @Mapping(target = "id", expression = "java(new OrderId(entity.getId()))")
    @Mapping(target = "customerId", expression = "java(new CustomerId(entity.getCustomerId()))")
    @Mapping(target = "totalAmount", expression = "java(new Money(entity.getTotalAmount()))")
    Order toDomain(OrderEntity entity);
} 