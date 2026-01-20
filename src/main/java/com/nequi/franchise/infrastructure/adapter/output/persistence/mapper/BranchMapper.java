package com.nequi.franchise.infrastructure.adapter.output.persistence.mapper;

import com.nequi.franchise.domain.model.Branch;
import com.nequi.franchise.infrastructure.adapter.output.persistence.entity.BranchEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface BranchMapper {
    Branch toDomain(BranchEntity entity);
    BranchEntity toEntity(Branch domain);
}
