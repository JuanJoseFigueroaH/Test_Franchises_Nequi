package com.nequi.franchise.application.service;

import com.nequi.franchise.domain.exception.BranchNotFoundException;
import com.nequi.franchise.domain.exception.FranchiseNotFoundException;
import com.nequi.franchise.domain.exception.ProductNotFoundException;
import com.nequi.franchise.domain.model.Branch;
import com.nequi.franchise.domain.model.Franchise;
import com.nequi.franchise.domain.model.Product;
import com.nequi.franchise.domain.port.input.UpdateProductNameUseCase;
import com.nequi.franchise.domain.port.output.CachePort;
import com.nequi.franchise.domain.port.output.FranchiseRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class UpdateProductNameService implements UpdateProductNameUseCase {

    private static final Logger logger = LoggerFactory.getLogger(UpdateProductNameService.class);
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final FranchiseRepositoryPort franchiseRepository;
    private final CachePort cachePort;

    public UpdateProductNameService(FranchiseRepositoryPort franchiseRepository, CachePort cachePort) {
        this.franchiseRepository = franchiseRepository;
        this.cachePort = cachePort;
    }

    @Override
    public Mono<Franchise> execute(String franchiseId, String branchId, String productId, String newName) {
        logger.info("Updating name of product '{}' in branch '{}' of franchise '{}' to '{}'", productId, branchId, franchiseId, newName);

        return cachePort.delete("franchise:" + franchiseId)
                .doOnSuccess(deleted -> logger.debug("Cache invalidated for franchise: {}", franchiseId))
                .then(franchiseRepository.findById(franchiseId))
                .switchIfEmpty(Mono.error(new FranchiseNotFoundException("Franchise not found with id: " + franchiseId)))
                .flatMap(franchise -> {
                    Branch branch = franchise.findBranch(branchId);
                    Product product = branch.findProduct(productId);
                    product.updateName(newName);
                    franchise.incrementVersion();
                    return franchiseRepository.save(franchise);
                })
                .flatMap(updatedFranchise ->
                        cachePort.set("franchise:" + updatedFranchise.getId(), updatedFranchise, CACHE_TTL)
                                .doOnSuccess(cached -> logger.debug("Franchise re-cached after product name update"))
                                .doOnError(error -> logger.warn("Failed to cache franchise: {}", error.getMessage()))
                                .onErrorReturn(false)
                                .thenReturn(updatedFranchise))
                .doOnSuccess(franchise -> logger.info("Product name updated successfully: {}", productId))
                .doOnError(error -> logger.error("Error updating product name: {}", error.getMessage()));
    }
}
