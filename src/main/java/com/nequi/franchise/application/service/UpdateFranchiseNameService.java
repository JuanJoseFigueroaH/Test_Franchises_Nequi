package com.nequi.franchise.application.service;

import com.nequi.franchise.domain.exception.FranchiseNotFoundException;
import com.nequi.franchise.domain.model.Franchise;
import com.nequi.franchise.domain.port.input.UpdateFranchiseNameUseCase;
import com.nequi.franchise.domain.port.output.CachePort;
import com.nequi.franchise.domain.port.output.FranchiseRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class UpdateFranchiseNameService implements UpdateFranchiseNameUseCase {

    private static final Logger logger = LoggerFactory.getLogger(UpdateFranchiseNameService.class);
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final FranchiseRepositoryPort franchiseRepository;
    private final CachePort cachePort;

    public UpdateFranchiseNameService(FranchiseRepositoryPort franchiseRepository, CachePort cachePort) {
        this.franchiseRepository = franchiseRepository;
        this.cachePort = cachePort;
    }

    @Override
    public Mono<Franchise> execute(String franchiseId, String newName) {
        logger.info("Updating name of franchise '{}' to '{}'", franchiseId, newName);

        return cachePort.delete("franchise:" + franchiseId)
                .doOnSuccess(deleted -> logger.debug("Cache invalidated for franchise: {}", franchiseId))
                .then(franchiseRepository.findById(franchiseId))
                .switchIfEmpty(Mono.error(new FranchiseNotFoundException("Franchise not found with id: " + franchiseId)))
                .flatMap(franchise -> {
                    franchise.setName(newName);
                    return franchiseRepository.save(franchise);
                })
                .flatMap(updatedFranchise ->
                        cachePort.set("franchise:" + updatedFranchise.getId(), updatedFranchise, CACHE_TTL)
                                .doOnSuccess(cached -> logger.debug("Franchise re-cached after name update"))
                                .doOnError(error -> logger.warn("Failed to cache franchise: {}", error.getMessage()))
                                .onErrorReturn(false)
                                .thenReturn(updatedFranchise))
                .doOnSuccess(franchise -> logger.info("Franchise name updated successfully: {}", franchiseId))
                .doOnError(error -> logger.error("Error updating franchise name: {}", error.getMessage()));
    }
}
