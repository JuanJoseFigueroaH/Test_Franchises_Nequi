package com.nequi.franchise.application.service;

import com.nequi.franchise.domain.exception.BranchNotFoundException;
import com.nequi.franchise.domain.exception.FranchiseNotFoundException;
import com.nequi.franchise.domain.model.Branch;
import com.nequi.franchise.domain.model.Franchise;
import com.nequi.franchise.domain.model.Product;
import com.nequi.franchise.domain.port.input.AddProductToBranchUseCase;
import com.nequi.franchise.domain.port.output.CachePort;
import com.nequi.franchise.domain.port.output.FranchiseRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Service
public class AddProductToBranchService implements AddProductToBranchUseCase {

    private static final Logger logger = LoggerFactory.getLogger(AddProductToBranchService.class);
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final FranchiseRepositoryPort franchiseRepository;
    private final CachePort cachePort;

    public AddProductToBranchService(FranchiseRepositoryPort franchiseRepository, CachePort cachePort) {
        this.franchiseRepository = franchiseRepository;
        this.cachePort = cachePort;
    }

    @Override
    public Mono<Franchise> execute(String franchiseId, String branchId, String productName, Integer stock) {
        logger.info("Adding product '{}' with stock {} to branch '{}' in franchise '{}'", productName, stock, branchId, franchiseId);

        return cachePort.delete("franchise:" + franchiseId)
                .doOnSuccess(deleted -> logger.debug("Cache invalidated for franchise: {}", franchiseId))
                .then(franchiseRepository.findById(franchiseId))
                .switchIfEmpty(Mono.error(new FranchiseNotFoundException("Franchise not found with id: " + franchiseId)))
                .flatMap(franchise -> {
                    Branch branch = franchise.findBranch(branchId);

                    Product newProduct = Product.builder()
                            .id(UUID.randomUUID().toString())
                            .name(productName)
                            .stock(stock)
                            .build();

                    branch.addProduct(newProduct);
                    franchise.incrementVersion();
                    return franchiseRepository.save(franchise);
                })
                .flatMap(updatedFranchise ->
                        cachePort.set("franchise:" + updatedFranchise.getId(), updatedFranchise, CACHE_TTL)
                                .doOnSuccess(cached -> logger.debug("Franchise re-cached after product addition"))
                                .doOnError(error -> logger.warn("Failed to cache franchise: {}", error.getMessage()))
                                .onErrorReturn(false)
                                .thenReturn(updatedFranchise))
                .doOnSuccess(franchise -> logger.info("Product added successfully to branch: {}", branchId))
                .doOnError(error -> logger.error("Error adding product to branch: {}", error.getMessage()));
    }
}
