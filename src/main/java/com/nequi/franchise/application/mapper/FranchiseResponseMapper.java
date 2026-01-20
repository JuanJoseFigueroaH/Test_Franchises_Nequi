package com.nequi.franchise.application.mapper;

import com.nequi.franchise.application.dto.FranchiseResponse;
import com.nequi.franchise.domain.model.Franchise;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {BranchResponseMapper.class})
public interface FranchiseResponseMapper {
    FranchiseResponse toResponse(Franchise franchise);
}
