package com.nequi.franchise.application.service;

import com.nequi.franchise.domain.model.Franchise;
import com.nequi.franchise.domain.port.input.CreateFranchiseUseCase;
import com.nequi.franchise.domain.port.output.CachePort;
import com.nequi.franchise.domain.port.output.FranchiseRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class CreateFranchiseService implements CreateFranchiseUseCase {

    private static final Logger logger = LoggerFactory.getLogger(CreateFranchiseService.class);
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final FranchiseRepositoryPort franchiseRepository;
    private final CachePort cachePort;

    public CreateFranchiseService(FranchiseRepositoryPort franchiseRepository, CachePort cachePort) {
        this.franchiseRepository = franchiseRepository;
        this.cachePort = cachePort;
    }

    @Override
    public Mono<Franchise> execute(String name) {
        logger.info("Creating franchise with name: {}", name);
        
        return Mono.just(Franchise.builder()
                        .id(UUID.randomUUID().toString())
                        .name(name)
                        .branches(new ArrayList<>())
                        .build())
                .flatMap(franchiseRepository::save)
                .flatMap(savedFranchise -> 
                    cachePort.set("franchise:" + savedFranchise.getId(), savedFranchise, CACHE_TTL)
                            .doOnSuccess(cached -> logger.debug("Franchise cached with key: franchise:{}", savedFranchise.getId()))
                            .doOnError(error -> logger.warn("Failed to cache franchise: {}", error.getMessage()))
                            .onErrorReturn(false)
                            .thenReturn(savedFranchise))
                .doOnSuccess(franchise -> logger.info("Franchise created successfully with id: {}", franchise.getId()))
                .doOnError(error -> logger.error("Error creating franchise: {}", error.getMessage()));
    }
}
