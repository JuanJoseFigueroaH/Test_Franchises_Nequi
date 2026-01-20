package com.nequi.franchise.application.service;

import com.nequi.franchise.domain.exception.BranchNotFoundException;
import com.nequi.franchise.domain.exception.FranchiseNotFoundException;
import com.nequi.franchise.domain.exception.ProductNotFoundException;
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
class DeleteProductFromBranchServiceTest {

    @Mock
    private FranchiseRepositoryPort franchiseRepository;

    @Mock
    private CachePort cachePort;

    @InjectMocks
    private DeleteProductFromBranchService deleteProductFromBranchService;

    private Franchise existingFranchise;
    private Branch existingBranch;
    private Product existingProduct;

    @BeforeEach
    void setUp() {
        existingProduct = Product.builder()
                .id("product-id")
                .name("Test Product")
                .stock(100)
                .build();

        existingBranch = Branch.builder()
                .id("branch-id")
                .name("Test Branch")
                .products(new ArrayList<>(List.of(existingProduct)))
                .build();

        existingFranchise = Franchise.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(new ArrayList<>(List.of(existingBranch)))
                .build();
    }

    @Test
    void execute_ShouldDeleteProductSuccessfully() {
        Franchise updatedFranchise = Franchise.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(new ArrayList<>())
                .build();
        Branch branchWithoutProduct = Branch.builder()
                .id("branch-id")
                .name("Test Branch")
                .products(new ArrayList<>())
                .build();
        updatedFranchise.getBranches().add(branchWithoutProduct);

        when(franchiseRepository.findById("franchise-id")).thenReturn(Mono.just(existingFranchise));
        when(franchiseRepository.save(any(Franchise.class))).thenReturn(Mono.just(updatedFranchise));
        when(cachePort.set(anyString(), any(Franchise.class), any(Duration.class))).thenReturn(Mono.just(true));

        Mono<Franchise> result = deleteProductFromBranchService.execute("franchise-id", "branch-id", "product-id");

        StepVerifier.create(result)
                .expectNextMatches(franchise ->
                        franchise.getBranches().get(0).getProducts().isEmpty())
                .verifyComplete();

        verify(franchiseRepository, times(1)).findById("franchise-id");
        verify(franchiseRepository, times(1)).save(any(Franchise.class));
        verify(cachePort, times(1)).set(anyString(), any(Franchise.class), any(Duration.class));
    }

    @Test
    void execute_ShouldThrowExceptionWhenFranchiseNotFound() {
        when(franchiseRepository.findById("non-existent-id")).thenReturn(Mono.empty());

        Mono<Franchise> result = deleteProductFromBranchService.execute("non-existent-id", "branch-id", "product-id");

        StepVerifier.create(result)
                .expectError(FranchiseNotFoundException.class)
                .verify();

        verify(franchiseRepository, times(1)).findById("non-existent-id");
        verify(franchiseRepository, never()).save(any(Franchise.class));
        verify(cachePort, never()).set(anyString(), any(Franchise.class), any(Duration.class));
    }

    @Test
    void execute_ShouldThrowExceptionWhenBranchNotFound() {
        when(franchiseRepository.findById("franchise-id")).thenReturn(Mono.just(existingFranchise));

        Mono<Franchise> result = deleteProductFromBranchService.execute("franchise-id", "non-existent-branch", "product-id");

        StepVerifier.create(result)
                .expectError(BranchNotFoundException.class)
                .verify();

        verify(franchiseRepository, times(1)).findById("franchise-id");
        verify(franchiseRepository, never()).save(any(Franchise.class));
        verify(cachePort, never()).set(anyString(), any(Franchise.class), any(Duration.class));
    }

    @Test
    void execute_ShouldThrowExceptionWhenProductNotFound() {
        when(franchiseRepository.findById("franchise-id")).thenReturn(Mono.just(existingFranchise));

        Mono<Franchise> result = deleteProductFromBranchService.execute("franchise-id", "branch-id", "non-existent-product");

        StepVerifier.create(result)
                .expectError(ProductNotFoundException.class)
                .verify();

        verify(franchiseRepository, times(1)).findById("franchise-id");
        verify(franchiseRepository, never()).save(any(Franchise.class));
        verify(cachePort, never()).set(anyString(), any(Franchise.class), any(Duration.class));
    }

    @Test
    void execute_ShouldHandleRepositorySaveError() {
        when(franchiseRepository.findById("franchise-id")).thenReturn(Mono.just(existingFranchise));
        when(franchiseRepository.save(any(Franchise.class)))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        Mono<Franchise> result = deleteProductFromBranchService.execute("franchise-id", "branch-id", "product-id");

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(franchiseRepository, times(1)).findById("franchise-id");
        verify(franchiseRepository, times(1)).save(any(Franchise.class));
        verify(cachePort, never()).set(anyString(), any(Franchise.class), any(Duration.class));
    }

    @Test
    void execute_ShouldContinueWhenCacheFails() {
        Franchise updatedFranchise = Franchise.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(new ArrayList<>())
                .build();
        Branch branchWithoutProduct = Branch.builder()
                .id("branch-id")
                .name("Test Branch")
                .products(new ArrayList<>())
                .build();
        updatedFranchise.getBranches().add(branchWithoutProduct);

        when(franchiseRepository.findById("franchise-id")).thenReturn(Mono.just(existingFranchise));
        when(franchiseRepository.save(any(Franchise.class))).thenReturn(Mono.just(updatedFranchise));
        when(cachePort.set(anyString(), any(Franchise.class), any(Duration.class)))
                .thenReturn(Mono.just(false));

        Mono<Franchise> result = deleteProductFromBranchService.execute("franchise-id", "branch-id", "product-id");

        StepVerifier.create(result)
                .expectNextMatches(franchise -> franchise.getBranches().get(0).getProducts().isEmpty())
                .verifyComplete();

        verify(franchiseRepository, times(1)).findById("franchise-id");
        verify(franchiseRepository, times(1)).save(any(Franchise.class));
        verify(cachePort, times(1)).set(anyString(), any(Franchise.class), any(Duration.class));
    }

    @Test
    void execute_ShouldDeleteOneProductFromMultiple() {
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

        existingBranch.setProducts(new ArrayList<>(List.of(product1, product2)));

        Franchise updatedFranchise = Franchise.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(new ArrayList<>())
                .build();
        Branch branchWithOneProduct = Branch.builder()
                .id("branch-id")
                .name("Test Branch")
                .products(new ArrayList<>(List.of(product2)))
                .build();
        updatedFranchise.getBranches().add(branchWithOneProduct);

        when(franchiseRepository.findById("franchise-id")).thenReturn(Mono.just(existingFranchise));
        when(franchiseRepository.save(any(Franchise.class))).thenReturn(Mono.just(updatedFranchise));
        when(cachePort.set(anyString(), any(Franchise.class), any(Duration.class))).thenReturn(Mono.just(true));

        Mono<Franchise> result = deleteProductFromBranchService.execute("franchise-id", "branch-id", "product-1");

        StepVerifier.create(result)
                .expectNextMatches(franchise ->
                        franchise.getBranches().get(0).getProducts().size() == 1 &&
                        franchise.getBranches().get(0).getProducts().get(0).getId().equals("product-2"))
                .verifyComplete();

        verify(franchiseRepository, times(1)).findById("franchise-id");
        verify(franchiseRepository, times(1)).save(any(Franchise.class));
    }
}
