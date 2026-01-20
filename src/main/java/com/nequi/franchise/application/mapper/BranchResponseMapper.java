package com.nequi.franchise.application.mapper;

import com.nequi.franchise.application.dto.BranchResponse;
import com.nequi.franchise.domain.model.Branch;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ProductResponseMapper.class})
public interface BranchResponseMapper {
    BranchResponse toResponse(Branch branch);
}
