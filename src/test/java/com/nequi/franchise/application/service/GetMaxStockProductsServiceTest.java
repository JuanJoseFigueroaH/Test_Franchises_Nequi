package com.nequi.franchise.application.service;

import com.nequi.franchise.domain.exception.FranchiseNotFoundException;
import com.nequi.franchise.domain.model.Branch;
import com.nequi.franchise.domain.model.Franchise;
import com.nequi.franchise.domain.model.Product;
import com.nequi.franchise.domain.port.output.CachePort;
import com.nequi.franchise.domain.port.output.FranchiseRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetMaxStockProductsServiceTest {

    @Mock
    private FranchiseRepositoryPort franchiseRepository;

    @Mock
    private CachePort cachePort;

    @InjectMocks
    private GetMaxStockProductsService getMaxStockProductsService;

    private Franchise franchise;

    @BeforeEach
    void setUp() {
        Product product1 = Product.builder()
                .id("product-1")
                .name("Product 1")
                .stock(50)
                .build();

        Product product2 = Product.builder()
                .id("product-2")
                .name("Product 2")
                .stock(100)
                .build();

        Product product3 = Product.builder()
                .id("product-3")
                .name("Product 3")
                .stock(75)
                .build();

        Branch branch1 = Branch.builder()
                .id("branch-1")
                .name("Branch 1")
                .products(new ArrayList<>(List.of(product1, product2)))
                .build();

        Branch branch2 = Branch.builder()
                .id("branch-2")
                .name("Branch 2")
                .products(new ArrayList<>(List.of(product3)))
                .build();

        franchise = Franchise.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(new ArrayList<>(List.of(branch1, branch2)))
                .build();
    }

    @Test
    void execute_ShouldReturnMaxStockProductsPerBranch() {
        when(cachePort.get(anyString(), eq(Franchise.class))).thenReturn(Mono.empty());
        when(franchiseRepository.findById("franchise-id")).thenReturn(Mono.just(franchise));
        when(cachePort.set(anyString(), any(Franchise.class), any(Duration.class))).thenReturn(Mono.just(true));

        Mono<Franchise> result = getMaxStockProductsService.execute("franchise-id");

        StepVerifier.create(result)
                .expectNextMatches(f -> {
                    boolean branch1HasMaxProduct = f.getBranches().get(0).getProducts().size() == 1 &&
                            f.getBranches().get(0).getProducts().get(0).getId().equals("product-2") &&
                            f.getBranches().get(0).getProducts().get(0).getStock().equals(100);

                    boolean branch2HasMaxProduct = f.getBranches().get(1).getProducts().size() == 1 &&
                            f.getBranches().get(1).getProducts().get(0).getId().equals("product-3") &&
                            f.getBranches().get(1).getProducts().get(0).getStock().equals(75);

                    return branch1HasMaxProduct && branch2HasMaxProduct;
                })
                .verifyComplete();

        verify(cachePort, times(1)).get(anyString(), eq(Franchise.class));
        verify(franchiseRepository, times(1)).findById("franchise-id");
        verify(cachePort, times(1)).set(anyString(), any(Franchise.class), any(Duration.class));
    }

    @Test
    void execute_ShouldReturnFromCacheWhenAvailable() {
        Franchise cachedFranchise = Franchise.builder()
                .id("franchise-id")
                .name("Cached Franchise")
                .branches(new ArrayList<>())
                .build();

        when(cachePort.get(anyString(), eq(Franchise.class))).thenReturn(Mono.just(cachedFranchise));

        Mono<Franchise> result = getMaxStockProductsService.execute("franchise-id");

        StepVerifier.create(result)
                .expectNext(cachedFranchise)
                .verifyComplete();

        verify(cachePort, times(1)).get(anyString(), eq(Franchise.class));
        verify(franchiseRepository, never()).findById(anyString());
        verify(cachePort, never()).set(anyString(), any(Franchise.class), any(Duration.class));
    }

    @Test
    void execute_ShouldThrowExceptionWhenFranchiseNotFound() {
        when(cachePort.get(anyString(), eq(Franchise.class))).thenReturn(Mono.empty());
        when(franchiseRepository.findById("non-existent-id")).thenReturn(Mono.empty());

        Mono<Franchise> result = getMaxStockProductsService.execute("non-existent-id");

        StepVerifier.create(result)
                .expectError(FranchiseNotFoundException.class)
                .verify();

        verify(cachePort, times(1)).get(anyString(), eq(Franchise.class));
        verify(franchiseRepository, times(1)).findById("non-existent-id");
        verify(cachePort, never()).set(anyString(), any(Franchise.class), any(Duration.class));
    }

    @Test
    void execute_ShouldExcludeBranchesWithoutProducts() {
        Branch emptyBranch = Branch.builder()
                .id("empty-branch")
                .name("Empty Branch")
                .products(new ArrayList<>())
                .build();

        franchise.getBranches().add(emptyBranch);

        when(cachePort.get(anyString(), eq(Franchise.class))).thenReturn(Mono.empty());
        when(franchiseRepository.findById("franchise-id")).thenReturn(Mono.just(franchise));
        when(cachePort.set(anyString(), any(Franchise.class), any(Duration.class))).thenReturn(Mono.just(true));

        Mono<Franchise> result = getMaxStockProductsService.execute("franchise-id");

        StepVerifier.create(result)
                .expectNextMatches(f -> f.getBranches().size() == 2)
                .verifyComplete();

        verify(franchiseRepository, times(1)).findById("franchise-id");
    }

    @Test
    void execute_ShouldHandleSingleProductBranch() {
        Product singleProduct = Product.builder()
                .id("single-product")
                .name("Single Product")
                .stock(200)
                .build();

        Branch singleProductBranch = Branch.builder()
                .id("single-branch")
                .name("Single Branch")
                .products(new ArrayList<>(List.of(singleProduct)))
                .build();

        Franchise singleBranchFranchise = Franchise.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(new ArrayList<>(List.of(singleProductBranch)))
                .build();

        when(cachePort.get(anyString(), eq(Franchise.class))).thenReturn(Mono.empty());
        when(franchiseRepository.findById("franchise-id")).thenReturn(Mono.just(singleBranchFranchise));
        when(cachePort.set(anyString(), any(Franchise.class), any(Duration.class))).thenReturn(Mono.just(true));

        Mono<Franchise> result = getMaxStockProductsService.execute("franchise-id");

        StepVerifier.create(result)
                .expectNextMatches(f ->
                        f.getBranches().get(0).getProducts().size() == 1 &&
                        f.getBranches().get(0).getProducts().get(0).getId().equals("single-product"))
                .verifyComplete();

        verify(franchiseRepository, times(1)).findById("franchise-id");
    }

    @Test
    void execute_ShouldContinueWhenCacheSetFails() {
        when(cachePort.get(anyString(), eq(Franchise.class))).thenReturn(Mono.empty());
        when(franchiseRepository.findById("franchise-id")).thenReturn(Mono.just(franchise));
        when(cachePort.set(anyString(), any(Franchise.class), any(Duration.class)))
                .thenReturn(Mono.just(false));

        Mono<Franchise> result = getMaxStockProductsService.execute("franchise-id");

        StepVerifier.create(result)
                .expectNextMatches(f -> f.getBranches().size() == 2)
                .verifyComplete();

        verify(franchiseRepository, times(1)).findById("franchise-id");
        verify(cachePort, times(1)).set(anyString(), any(Franchise.class), any(Duration.class));
    }

    @Test
    void execute_ShouldHandleMultipleProductsWithSameMaxStock() {
        Product product1 = Product.builder()
                .id("product-1")
                .name("Product 1")
                .stock(100)
                .build();

        Product product2 = Product.builder()
                .id("product-2")
                .name("Product 2")
                .stock(100)
                .build();

        Branch branch = Branch.builder()
                .id("branch-1")
                .name("Branch 1")
                .products(new ArrayList<>(List.of(product1, product2)))
                .build();

        Franchise testFranchise = Franchise.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(new ArrayList<>(List.of(branch)))
                .build();

        when(cachePort.get(anyString(), eq(Franchise.class))).thenReturn(Mono.empty());
        when(franchiseRepository.findById("franchise-id")).thenReturn(Mono.just(testFranchise));
        when(cachePort.set(anyString(), any(Franchise.class), any(Duration.class))).thenReturn(Mono.just(true));

        Mono<Franchise> result = getMaxStockProductsService.execute("franchise-id");

        StepVerifier.create(result)
                .expectNextMatches(f ->
                        f.getBranches().get(0).getProducts().size() == 1 &&
                        f.getBranches().get(0).getProducts().get(0).getStock().equals(100))
                .verifyComplete();

        verify(franchiseRepository, times(1)).findById("franchise-id");
    }
}
