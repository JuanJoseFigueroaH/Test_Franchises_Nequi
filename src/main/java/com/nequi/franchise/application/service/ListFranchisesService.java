package com.nequi.franchise.application.service;

import com.nequi.franchise.domain.model.Franchise;
import com.nequi.franchise.domain.model.Page;
import com.nequi.franchise.domain.port.input.ListFranchisesUseCase;
import com.nequi.franchise.domain.port.output.FranchiseRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ListFranchisesService implements ListFranchisesUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ListFranchisesService.class);
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final FranchiseRepositoryPort franchiseRepository;

    public ListFranchisesService(FranchiseRepositoryPort franchiseRepository) {
        this.franchiseRepository = franchiseRepository;
    }

    @Override
    public Mono<Page<Franchise>> execute(Integer pageSize, String cursor) {
        int validatedPageSize = validatePageSize(pageSize);
        
        logger.info("Listing franchises with pageSize: {} and cursor: {}", validatedPageSize, cursor);

        return franchiseRepository.findAll(validatedPageSize, cursor)
                .doOnSuccess(page -> logger.info("Retrieved {} franchises, hasMore: {}", 
                    page.getPageSize(), page.getHasMore()))
                .doOnError(error -> logger.error("Error listing franchises: {}", error.getMessage()));
    }

    private int validatePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }
}
