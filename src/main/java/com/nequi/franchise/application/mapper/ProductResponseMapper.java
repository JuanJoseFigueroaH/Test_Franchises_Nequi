package com.nequi.franchise.application.mapper;

import com.nequi.franchise.application.dto.ProductResponse;
import com.nequi.franchise.domain.model.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductResponseMapper {
    ProductResponse toResponse(Product product);
}
