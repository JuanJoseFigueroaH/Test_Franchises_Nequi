package com.nequi.franchise.application.service;

import com.nequi.franchise.domain.exception.FranchiseNotFoundException;
import com.nequi.franchise.domain.model.Branch;
import com.nequi.franchise.domain.model.Franchise;
import com.nequi.franchise.domain.model.Product;
import com.nequi.franchise.domain.port.input.GetMaxStockProductsUseCase;
import com.nequi.franchise.domain.port.output.CachePort;
import com.nequi.franchise.domain.port.output.FranchiseRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class GetMaxStockProductsService implements GetMaxStockProductsUseCase {

    private static final Logger logger = LoggerFactory.getLogger(GetMaxStockProductsService.class);
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final FranchiseRepositoryPort franchiseRepository;
    private final CachePort cachePort;

    public GetMaxStockProductsService(FranchiseRepositoryPort franchiseRepository, CachePort cachePort) {
        this.franchiseRepository = franchiseRepository;
        this.cachePort = cachePort;
    }

    @Override
    public Mono<Franchise> execute(String franchiseId) {
        logger.info("Getting products with max stock per branch for franchise: {}", franchiseId);

        String cacheKey = "franchise:max-stock:" + franchiseId;

        return cachePort.get(cacheKey, Franchise.class)
                .switchIfEmpty(
                        franchiseRepository.findById(franchiseId)
                                .switchIfEmpty(Mono.error(new FranchiseNotFoundException("Franchise not found with id: " + franchiseId)))
                                .map(this::filterMaxStockProducts)
                                .flatMap(filteredFranchise ->
                                        cachePort.set(cacheKey, filteredFranchise, CACHE_TTL)
                                                .thenReturn(filteredFranchise))
                )
                .doOnSuccess(franchise -> logger.info("Retrieved max stock products for franchise: {}", franchiseId))
                .doOnError(error -> logger.error("Error getting max stock products: {}", error.getMessage()));
    }

    private Franchise filterMaxStockProducts(Franchise franchise) {
        List<Branch> filteredBranches = new ArrayList<>();

        for (Branch branch : franchise.getBranches()) {
            if (branch.getProducts().isEmpty()) {
                continue;
            }

            Product maxStockProduct = branch.getProducts().stream()
                    .max(Comparator.comparing(Product::getStock))
                    .orElse(null);

            if (maxStockProduct != null) {
                Branch filteredBranch = Branch.builder()
                        .id(branch.getId())
                        .name(branch.getName())
                        .products(List.of(maxStockProduct))
                        .build();
                filteredBranches.add(filteredBranch);
            }
        }

        return Franchise.builder()
                .id(franchise.getId())
                .name(franchise.getName())
                .branches(filteredBranches)
                .build();
    }
}
