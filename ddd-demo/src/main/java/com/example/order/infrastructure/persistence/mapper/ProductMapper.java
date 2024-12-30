package com.example.order.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.order.domain.model.Money;
import com.example.order.domain.model.product.Product;
import com.example.order.domain.model.product.ProductId;
import com.example.order.infrastructure.persistence.entity.ProductEntity;

@Mapper(componentModel = "spring", imports = {Money.class, ProductId.class})
public interface ProductMapper {
    
    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "price", source = "price.amount")
    ProductEntity toEntity(Product product);

    @Mapping(target = "id", expression = "java(new ProductId(entity.getId()))")
    @Mapping(target = "price", expression = "java(new Money(entity.getPrice()))")
    Product toDomain(ProductEntity entity);
} 