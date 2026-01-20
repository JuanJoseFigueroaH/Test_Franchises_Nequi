package com.nequi.franchise.infrastructure.adapter.output.persistence.mapper;

import com.nequi.franchise.domain.model.Franchise;
import com.nequi.franchise.infrastructure.adapter.output.persistence.entity.FranchiseEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {BranchMapper.class})
public interface FranchiseMapper {
    Franchise toDomain(FranchiseEntity entity);
    FranchiseEntity toEntity(Franchise domain);
}
