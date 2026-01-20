package com.nequi.franchise.application.service;

import com.nequi.franchise.domain.exception.BranchNotFoundException;
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
class AddProductToBranchServiceTest {

    @Mock
    private FranchiseRepositoryPort franchiseRepository;

    @Mock
    private CachePort cachePort;

    @InjectMocks
    private AddProductToBranchService addProductToBranchService;

    private Franchise existingFranchise;
    private Branch existingBranch;

    @BeforeEach
    void setUp() {
        existingBranch = Branch.builder()
                .id("branch-id")
                .name("Test Branch")
                .products(new ArrayList<>())
                .build();

        existingFranchise = Franchise.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(new ArrayList<>(List.of(existingBranch)))
                .build();
    }

    @Test
    void execute_ShouldAddProductSuccessfully() {
        Product newProduct = Product.builder()
                .id("product-id")
                .name("Test Product")
                .stock(100)
                .build();

        Franchise updatedFranchise = Franchise.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(new ArrayList<>())
                .build();
        Branch branchWithProduct = Branch.builder()
                .id("branch-id")
                .name("Test Branch")
                .products(new ArrayList<>(List.of(newProduct)))
                .build();
        updatedFranchise.getBranches().add(branchWithProduct);

        when(franchiseRepository.findById("franchise-id")).thenReturn(Mono.just(existingFranchise));
        when(franchiseRepository.save(any(Franchise.class))).thenReturn(Mono.just(updatedFranchise));
        when(cachePort.set(anyString(), any(Franchise.class), any(Duration.class))).thenReturn(Mono.just(true));

        Mono<Franchise> result = addProductToBranchService.execute("franchise-id", "branch-id", "Test Product", 100);

        StepVerifier.create(result)
                .expectNextMatches(franchise ->
                        franchise.getBranches().get(0).getProducts().size() == 1 &&
                        franchise.getBranches().get(0).getProducts().get(0).getName().equals("Test Product") &&
                        franchise.getBranches().get(0).getProducts().get(0).getStock().equals(100))
                .verifyComplete();

        verify(franchiseRepository, times(1)).findById("franchise-id");
        verify(franchiseRepository, times(1)).save(any(Franchise.class));
        verify(cachePort, times(1)).set(anyString(), any(Franchise.class), any(Duration.class));
    }

    @Test
    void execute_ShouldThrowExceptionWhenFranchiseNotFound() {
        when(franchiseRepository.findById("non-existent-id")).thenReturn(Mono.empty());

        Mono<Franchise> result = addProductToBranchService.execute("non-existent-id", "branch-id", "Test Product", 100);

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

        Mono<Franchise> result = addProductToBranchService.execute("franchise-id", "non-existent-branch", "Test Product", 100);

        StepVerifier.create(result)
                .expectError(BranchNotFoundException.class)
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

        Mono<Franchise> result = addProductToBranchService.execute("franchise-id", "branch-id", "Test Product", 100);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(franchiseRepository, times(1)).findById("franchise-id");
        verify(franchiseRepository, times(1)).save(any(Franchise.class));
        verify(cachePort, never()).set(anyString(), any(Franchise.class), any(Duration.class));
    }

    @Test
    void execute_ShouldContinueWhenCacheFails() {
        Product newProduct = Product.builder()
                .id("product-id")
                .name("Test Product")
                .stock(100)
                .build();

        Franchise updatedFranchise = Franchise.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(new ArrayList<>())
                .build();
        Branch branchWithProduct = Branch.builder()
                .id("branch-id")
                .name("Test Branch")
                .products(new ArrayList<>(List.of(newProduct)))
                .build();
        updatedFranchise.getBranches().add(branchWithProduct);

        when(franchiseRepository.findById("franchise-id")).thenReturn(Mono.just(existingFranchise));
        when(franchiseRepository.save(any(Franchise.class))).thenReturn(Mono.just(updatedFranchise));
        when(cachePort.set(anyString(), any(Franchise.class), any(Duration.class)))
                .thenReturn(Mono.just(false));

        Mono<Franchise> result = addProductToBranchService.execute("franchise-id", "branch-id", "Test Product", 100);

        StepVerifier.create(result)
                .expectNextMatches(franchise -> franchise.getBranches().get(0).getProducts().size() == 1)
                .verifyComplete();

        verify(franchiseRepository, times(1)).findById("franchise-id");
        verify(franchiseRepository, times(1)).save(any(Franchise.class));
        verify(cachePort, times(1)).set(anyString(), any(Franchise.class), any(Duration.class));
    }

    @Test
    void execute_ShouldAddProductToExistingProducts() {
        Product existingProduct = Product.builder()
                .id("existing-product-id")
                .name("Existing Product")
                .stock(50)
                .build();
        existingBranch.getProducts().add(existingProduct);

        Product newProduct = Product.builder()
                .id("new-product-id")
                .name("New Product")
                .stock(100)
                .build();

        Franchise updatedFranchise = Franchise.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(new ArrayList<>())
                .build();
        Branch branchWithProducts = Branch.builder()
                .id("branch-id")
                .name("Test Branch")
                .products(new ArrayList<>(List.of(existingProduct, newProduct)))
                .build();
        updatedFranchise.getBranches().add(branchWithProducts);

        when(franchiseRepository.findById("franchise-id")).thenReturn(Mono.just(existingFranchise));
        when(franchiseRepository.save(any(Franchise.class))).thenReturn(Mono.just(updatedFranchise));
        when(cachePort.set(anyString(), any(Franchise.class), any(Duration.class))).thenReturn(Mono.just(true));

        Mono<Franchise> result = addProductToBranchService.execute("franchise-id", "branch-id", "New Product", 100);

        StepVerifier.create(result)
                .expectNextMatches(franchise -> franchise.getBranches().get(0).getProducts().size() == 2)
                .verifyComplete();

        verify(franchiseRepository, times(1)).findById("franchise-id");
        verify(franchiseRepository, times(1)).save(any(Franchise.class));
    }

    @Test
    void execute_ShouldAddProductWithZeroStock() {
        Product newProduct = Product.builder()
                .id("product-id")
                .name("Test Product")
                .stock(0)
                .build();

        Franchise updatedFranchise = Franchise.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(new ArrayList<>())
                .build();
        Branch branchWithProduct = Branch.builder()
                .id("branch-id")
                .name("Test Branch")
                .products(new ArrayList<>(List.of(newProduct)))
                .build();
        updatedFranchise.getBranches().add(branchWithProduct);

        when(franchiseRepository.findById("franchise-id")).thenReturn(Mono.just(existingFranchise));
        when(franchiseRepository.save(any(Franchise.class))).thenReturn(Mono.just(updatedFranchise));
        when(cachePort.set(anyString(), any(Franchise.class), any(Duration.class))).thenReturn(Mono.just(true));

        Mono<Franchise> result = addProductToBranchService.execute("franchise-id", "branch-id", "Test Product", 0);

        StepVerifier.create(result)
                .expectNextMatches(franchise ->
                        franchise.getBranches().get(0).getProducts().get(0).getStock().equals(0))
                .verifyComplete();

        verify(franchiseRepository, times(1)).findById("franchise-id");
        verify(franchiseRepository, times(1)).save(any(Franchise.class));
    }
}
