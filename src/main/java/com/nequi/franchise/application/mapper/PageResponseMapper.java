package com.nequi.franchise.application.mapper;

import com.nequi.franchise.application.dto.FranchiseResponse;
import com.nequi.franchise.application.dto.PageResponse;
import com.nequi.franchise.domain.model.Franchise;
import com.nequi.franchise.domain.model.Page;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PageResponseMapper {

    private final FranchiseResponseMapper franchiseResponseMapper;

    public PageResponseMapper(FranchiseResponseMapper franchiseResponseMapper) {
        this.franchiseResponseMapper = franchiseResponseMapper;
    }

    public PageResponse<FranchiseResponse> toPageResponse(Page<Franchise> page) {
        return PageResponse.<FranchiseResponse>builder()
                .items(page.getItems().stream()
                        .map(franchiseResponseMapper::toResponse)
                        .collect(Collectors.toList()))
                .pageSize(page.getPageSize())
                .nextCursor(page.getNextCursor())
                .hasMore(page.getHasMore())
                .build();
    }
}
