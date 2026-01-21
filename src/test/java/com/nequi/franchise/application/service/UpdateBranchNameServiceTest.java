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
class UpdateBranchNameServiceTest {

    @Mock
    private FranchiseRepositoryPort franchiseRepository;

    @Mock
    private CachePort cachePort;

    @InjectMocks
    private UpdateBranchNameService updateBranchNameService;

    private Franchise existingFranchise;
    private Branch existingBranch;

    @BeforeEach
    void setUp() {
        existingBranch = Branch.builder()
                .id("branch-id")
                .name("Old Branch Name")
                .products(new ArrayList<>())
                .build();

        existingFranchise = Franchise.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(new ArrayList<>(List.of(existingBranch)))
                .build();
    }

    @Test
    void execute_ShouldUpdateNameSuccessfully() {
        Branch updatedBranch = Branch.builder()
                .id("branch-id")
                .name("New Branch Name")
                .products(new ArrayList<>())
                .build();

        Franchise updatedFranchise = Franchise.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(new ArrayList<>(List.of(updatedBranch)))
                .build();

        when(franchiseRepository.findById("franchise-id")).thenReturn(Mono.just(existingFranchise));
        when(franchiseRepository.save(any(Franchise.class))).thenReturn(Mono.just(updatedFranchise));
        when(cachePort.set(anyString(), any(Franchise.class), any(Duration.class))).thenReturn(Mono.just(true));

        Mono<Franchise> result = updateBranchNameService.execute("franchise-id", "branch-id", "New Branch Name");

        StepVerifier.create(result)
                .expectNextMatches(franchise ->
                        franchise.getBranches().get(0).getName().equals("New Branch Name"))
                .verifyComplete();

        verify(franchiseRepository, times(1)).findById("franchise-id");
        verify(franchiseRepository, times(1)).save(any(Franchise.class));
        verify(cachePort, times(1)).set(anyString(), any(Franchise.class), any(Duration.class));
    }

    @Test
    void execute_ShouldThrowExceptionWhenFranchiseNotFound() {
        when(franchiseRepository.findById("non-existent-id")).thenReturn(Mono.empty());

        Mono<Franchise> result = updateBranchNameService.execute("non-existent-id", "branch-id", "New Name");

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

        Mono<Franchise> result = updateBranchNameService.execute("franchise-id", "non-existent-branch", "New Name");

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

        Mono<Franchise> result = updateBranchNameService.execute("franchise-id", "branch-id", "New Name");

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(franchiseRepository, times(1)).findById("franchise-id");
        verify(franchiseRepository, times(1)).save(any(Franchise.class));
        verify(cachePort, never()).set(anyString(), any(Franchise.class), any(Duration.class));
    }

    @Test
    void execute_ShouldContinueWhenCacheFails() {
        Branch updatedBranch = Branch.builder()
                .id("branch-id")
                .name("New Branch Name")
                .products(new ArrayList<>())
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

        Mono<Franchise> result = updateBranchNameService.execute("franchise-id", "branch-id", "New Branch Name");

        StepVerifier.create(result)
                .expectNextMatches(franchise ->
                        franchise.getBranches().get(0).getName().equals("New Branch Name"))
                .verifyComplete();

        verify(franchiseRepository, times(1)).findById("franchise-id");
        verify(franchiseRepository, times(1)).save(any(Franchise.class));
        verify(cachePort, times(1)).set(anyString(), any(Franchise.class), any(Duration.class));
    }

    @Test
    void execute_ShouldUpdateNameWithProducts() {
        Product product = Product.builder()
                .id("product-id")
                .name("Test Product")
                .stock(100)
                .build();
        existingBranch.getProducts().add(product);

        Branch updatedBranch = Branch.builder()
                .id("branch-id")
                .name("Updated Branch Name")
                .products(new ArrayList<>(List.of(product)))
                .build();

        Franchise updatedFranchise = Franchise.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(new ArrayList<>(List.of(updatedBranch)))
                .build();

        when(franchiseRepository.findById("franchise-id")).thenReturn(Mono.just(existingFranchise));
        when(franchiseRepository.save(any(Franchise.class))).thenReturn(Mono.just(updatedFranchise));
        when(cachePort.set(anyString(), any(Franchise.class), any(Duration.class))).thenReturn(Mono.just(true));

        Mono<Franchise> result = updateBranchNameService.execute("franchise-id", "branch-id", "Updated Branch Name");

        StepVerifier.create(result)
                .expectNextMatches(franchise ->
                        franchise.getBranches().get(0).getName().equals("Updated Branch Name") &&
                        franchise.getBranches().get(0).getProducts().size() == 1)
                .verifyComplete();

        verify(franchiseRepository, times(1)).findById("franchise-id");
        verify(franchiseRepository, times(1)).save(any(Franchise.class));
    }
}
