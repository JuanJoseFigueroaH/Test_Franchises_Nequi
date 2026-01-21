package com.nequi.franchise.application.service;

import com.nequi.franchise.domain.exception.BranchNotFoundException;
import com.nequi.franchise.domain.exception.FranchiseNotFoundException;
import com.nequi.franchise.domain.exception.ProductNotFoundException;
import com.nequi.franchise.domain.model.Branch;
import com.nequi.franchise.domain.model.Franchise;
import com.nequi.franchise.domain.model.Product;
import com.nequi.franchise.domain.port.input.UpdateProductStockUseCase;
import com.nequi.franchise.domain.port.output.CachePort;
import com.nequi.franchise.domain.port.output.FranchiseRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class UpdateProductStockService implements UpdateProductStockUseCase {

    private static final Logger logger = LoggerFactory.getLogger(UpdateProductStockService.class);
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final FranchiseRepositoryPort franchiseRepository;
    private final CachePort cachePort;

    public UpdateProductStockService(FranchiseRepositoryPort franchiseRepository, CachePort cachePort) {
        this.franchiseRepository = franchiseRepository;
        this.cachePort = cachePort;
    }

    @Override
    public Mono<Franchise> execute(String franchiseId, String branchId, String productId, Integer newStock) {
        logger.info("Updating stock of product '{}' in branch '{}' of franchise '{}' to {}", productId, branchId, franchiseId, newStock);

        return cachePort.delete("franchise:" + franchiseId)
                .doOnSuccess(deleted -> logger.debug("Cache invalidated for franchise: {}", franchiseId))
                .then(franchiseRepository.findById(franchiseId))
                .switchIfEmpty(Mono.error(new FranchiseNotFoundException("Franchise not found with id: " + franchiseId)))
                .flatMap(franchise -> {
                    Branch branch = franchise.findBranch(branchId);
                    Product product = branch.findProduct(productId);
                    product.updateStock(newStock);
                    franchise.incrementVersion();
                    return franchiseRepository.save(franchise);
                })
                .flatMap(updatedFranchise ->
                        cachePort.set("franchise:" + updatedFranchise.getId(), updatedFranchise, CACHE_TTL)
                                .doOnSuccess(cached -> logger.debug("Franchise re-cached after stock update"))
                                .doOnError(error -> logger.warn("Failed to cache franchise: {}", error.getMessage()))
                                .onErrorReturn(false)
                                .thenReturn(updatedFranchise))
                .doOnSuccess(franchise -> logger.info("Product stock updated successfully: {}", productId))
                .doOnError(error -> logger.error("Error updating product stock: {}", error.getMessage()));
    }
}
