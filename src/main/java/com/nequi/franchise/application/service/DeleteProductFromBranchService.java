package com.nequi.franchise.application.service;

import com.nequi.franchise.domain.exception.BranchNotFoundException;
import com.nequi.franchise.domain.exception.FranchiseNotFoundException;
import com.nequi.franchise.domain.exception.ProductNotFoundException;
import com.nequi.franchise.domain.model.Branch;
import com.nequi.franchise.domain.model.Franchise;
import com.nequi.franchise.domain.port.input.DeleteProductFromBranchUseCase;
import com.nequi.franchise.domain.port.output.CachePort;
import com.nequi.franchise.domain.port.output.FranchiseRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class DeleteProductFromBranchService implements DeleteProductFromBranchUseCase {

    private static final Logger logger = LoggerFactory.getLogger(DeleteProductFromBranchService.class);
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final FranchiseRepositoryPort franchiseRepository;
    private final CachePort cachePort;

    public DeleteProductFromBranchService(FranchiseRepositoryPort franchiseRepository, CachePort cachePort) {
        this.franchiseRepository = franchiseRepository;
        this.cachePort = cachePort;
    }

    @Override
    public Mono<Franchise> execute(String franchiseId, String branchId, String productId) {
        logger.info("Deleting product '{}' from branch '{}' in franchise '{}'", productId, branchId, franchiseId);

        return cachePort.delete("franchise:" + franchiseId)
                .doOnSuccess(deleted -> logger.debug("Cache invalidated for franchise: {}", franchiseId))
                .then(franchiseRepository.findById(franchiseId))
                .switchIfEmpty(Mono.error(new FranchiseNotFoundException("Franchise not found with id: " + franchiseId)))
                .flatMap(franchise -> {
                    Branch branch = franchise.getBranches().stream()
                            .filter(b -> b.getId().equals(branchId))
                            .findFirst()
                            .orElseThrow(() -> new BranchNotFoundException("Branch not found with id: " + branchId));

                    boolean productRemoved = branch.getProducts().removeIf(p -> p.getId().equals(productId));
                    if (!productRemoved) {
                        throw new ProductNotFoundException("Product not found with id: " + productId);
                    }

                    return franchiseRepository.save(franchise);
                })
                .flatMap(updatedFranchise ->
                        cachePort.set("franchise:" + updatedFranchise.getId(), updatedFranchise, CACHE_TTL)
                                .doOnSuccess(cached -> logger.debug("Franchise re-cached after product deletion"))
                                .doOnError(error -> logger.warn("Failed to cache franchise: {}", error.getMessage()))
                                .onErrorReturn(false)
                                .thenReturn(updatedFranchise))
                .doOnSuccess(franchise -> logger.info("Product deleted successfully from branch: {}", branchId))
                .doOnError(error -> logger.error("Error deleting product from branch: {}", error.getMessage()));
    }
}
