package com.nequi.franchise.infrastructure.adapter.output.persistence.mapper;

import com.nequi.franchise.domain.model.Product;
import com.nequi.franchise.infrastructure.adapter.output.persistence.entity.ProductEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toDomain(ProductEntity entity);
    ProductEntity toEntity(Product domain);
}
