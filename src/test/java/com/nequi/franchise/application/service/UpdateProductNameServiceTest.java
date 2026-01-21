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
class UpdateProductNameServiceTest {

    @Mock
    private FranchiseRepositoryPort franchiseRepository;

    @Mock
    private CachePort cachePort;

    @InjectMocks
    private UpdateProductNameService updateProductNameService;

    private Franchise existingFranchise;
    private Branch existingBranch;
    private Product existingProduct;

    @BeforeEach
    void setUp() {
        existingProduct = Product.builder()
                .id("product-id")
                .name("Old Product Name")
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
    void execute_ShouldUpdateNameSuccessfully() {
        Product updatedProduct = Product.builder()
                .id("product-id")
                .name("New Product Name")
                .stock(100)
                .build();

        Branch updatedBranch = Branch.builder()
                .id("branch-id")
                .name("Test Branch")
                .products(new ArrayList<>(List.of(updatedProduct)))
                .build();

        Franchise updatedFranchise = Franchise.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(new ArrayList<>(List.of(updatedBranch)))
                .build();

        when(franchiseRepository.findById("franchise-id")).thenReturn(Mono.just(existingFranchise));
        when(franchiseRepository.save(any(Franchise.class))).thenReturn(Mono.just(updatedFranchise));
        when(cachePort.set(anyString(), any(Franchise.class), any(Duration.class))).thenReturn(Mono.just(true));

        Mono<Franchise> result = updateProductNameService.execute("franchise-id", "branch-id", "product-id", "New Product Name");

        StepVerifier.create(result)
                .expectNextMatches(franchise ->
                        franchise.getBranches().get(0).getProducts().get(0).getName().equals("New Product Name"))
                .verifyComplete();

        verify(franchiseRepository, times(1)).findById("franchise-id");
        verify(franchiseRepository, times(1)).save(any(Franchise.class));
        verify(cachePort, times(1)).set(anyString(), any(Franchise.class), any(Duration.class));
    }

    @Test
    void execute_ShouldThrowExceptionWhenFranchiseNotFound() {
        when(franchiseRepository.findById("non-existent-id")).thenReturn(Mono.empty());

        Mono<Franchise> result = updateProductNameService.execute("non-existent-id", "branch-id", "product-id", "New Name");

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

        Mono<Franchise> result = updateProductNameService.execute("franchise-id", "non-existent-branch", "product-id", "New Name");

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

        Mono<Franchise> result = updateProductNameService.execute("franchise-id", "branch-id", "non-existent-product", "New Name");

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

        Mono<Franchise> result = updateProductNameService.execute("franchise-id", "branch-id", "product-id", "New Name");

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(franchiseRepository, times(1)).findById("franchise-id");
        verify(franchiseRepository, times(1)).save(any(Franchise.class));
        verify(cachePort, never()).set(anyString(), any(Franchise.class), any(Duration.class));
    }

    @Test
    void execute_ShouldContinueWhenCacheFails() {
        Product updatedProduct = Product.builder()
                .id("product-id")
                .name("New Product Name")
                .stock(100)
                .build();

        Branch updatedBranch = Branch.builder()
                .id("branch-id")
                .name("Test Branch")
                .products(new ArrayList<>(List.of(updatedProduct)))
                .build();

        Franchise updatedFranchise = Franchise.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(new ArrayList<>(List.of(updatedBranch)))
                .build();

        when(franchiseRepository.findById("franchise-id")).thenReturn(Mono.just(existingFranchise));
        when(franchiseRepository.save(any(Franchise.class))).thenReturn(Mono.just(updatedFranchise));
        when(cachePort.set(anyString(), any(Franchise.class), any(Duration.class)))
                .thenReturn(Mono.just(false));

        Mono<Franchise> result = updateProductNameService.execute("franchise-id", "branch-id", "product-id", "New Product Name");

        StepVerifier.create(result)
                .expectNextMatches(franchise ->
                        franchise.getBranches().get(0).getProducts().get(0).getName().equals("New Product Name"))
                .verifyComplete();

        verify(franchiseRepository, times(1)).findById("franchise-id");
        verify(franchiseRepository, times(1)).save(any(Franchise.class));
        verify(cachePort, times(1)).set(anyString(), any(Franchise.class), any(Duration.class));
    }

    @Test
    void execute_ShouldUpdateNameAndPreserveStock() {
        Product updatedProduct = Product.builder()
                .id("product-id")
                .name("Updated Product Name")
                .stock(100)
                .build();

        Branch updatedBranch = Branch.builder()
                .id("branch-id")
                .name("Test Branch")
                .products(new ArrayList<>(List.of(updatedProduct)))
                .build();

        Franchise updatedFranchise = Franchise.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(new ArrayList<>(List.of(updatedBranch)))
                .build();

        when(franchiseRepository.findById("franchise-id")).thenReturn(Mono.just(existingFranchise));
        when(franchiseRepository.save(any(Franchise.class))).thenReturn(Mono.just(updatedFranchise));
        when(cachePort.set(anyString(), any(Franchise.class), any(Duration.class))).thenReturn(Mono.just(true));

        Mono<Franchise> result = updateProductNameService.execute("franchise-id", "branch-id", "product-id", "Updated Product Name");

        StepVerifier.create(result)
                .expectNextMatches(franchise -> {
                    Product product = franchise.getBranches().get(0).getProducts().get(0);
                    return product.getName().equals("Updated Product Name") &&
                           product.getStock().equals(100);
                })
                .verifyComplete();

        verify(franchiseRepository, times(1)).findById("franchise-id");
        verify(franchiseRepository, times(1)).save(any(Franchise.class));
    }
}
