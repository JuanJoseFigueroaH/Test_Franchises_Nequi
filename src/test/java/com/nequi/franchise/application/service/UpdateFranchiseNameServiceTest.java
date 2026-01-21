package com.nequi.franchise.application.service;

import com.nequi.franchise.domain.exception.FranchiseNotFoundException;
import com.nequi.franchise.domain.model.Branch;
import com.nequi.franchise.domain.model.Franchise;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateFranchiseNameServiceTest {

    @Mock
    private FranchiseRepositoryPort franchiseRepository;

    @Mock
    private CachePort cachePort;

    @InjectMocks
    private UpdateFranchiseNameService updateFranchiseNameService;

    private Franchise existingFranchise;

    @BeforeEach
    void setUp() {
        existingFranchise = Franchise.builder()
                .id("franchise-id")
                .name("Old Franchise Name")
                .branches(new ArrayList<>())
                .build();
    }

    @Test
    void execute_ShouldUpdateNameSuccessfully() {
        Franchise updatedFranchise = Franchise.builder()
                .id("franchise-id")
                .name("New Franchise Name")
                .branches(new ArrayList<>())
                .build();

        when(franchiseRepository.findById("franchise-id")).thenReturn(Mono.just(existingFranchise));
        when(franchiseRepository.save(any(Franchise.class))).thenReturn(Mono.just(updatedFranchise));
        when(cachePort.set(anyString(), any(Franchise.class), any(Duration.class))).thenReturn(Mono.just(true));

        Mono<Franchise> result = updateFranchiseNameService.execute("franchise-id", "New Franchise Name");

        StepVerifier.create(result)
                .expectNextMatches(franchise -> franchise.getName().equals("New Franchise Name"))
                .verifyComplete();

        verify(franchiseRepository, times(1)).findById("franchise-id");
        verify(franchiseRepository, times(1)).save(any(Franchise.class));
        verify(cachePort, times(1)).set(anyString(), any(Franchise.class), any(Duration.class));
    }

    @Test
    void execute_ShouldThrowExceptionWhenFranchiseNotFound() {
        when(franchiseRepository.findById("non-existent-id")).thenReturn(Mono.empty());

        Mono<Franchise> result = updateFranchiseNameService.execute("non-existent-id", "New Name");

        StepVerifier.create(result)
                .expectError(FranchiseNotFoundException.class)
                .verify();

        verify(franchiseRepository, times(1)).findById("non-existent-id");
        verify(franchiseRepository, never()).save(any(Franchise.class));
        verify(cachePort, never()).set(anyString(), any(Franchise.class), any(Duration.class));
    }

    @Test
    void execute_ShouldHandleRepositorySaveError() {
        when(franchiseRepository.findById("franchise-id")).thenReturn(Mono.just(existingFranchise));
        when(franchiseRepository.save(any(Franchise.class)))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        Mono<Franchise> result = updateFranchiseNameService.execute("franchise-id", "New Name");

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
                .name("New Franchise Name")
                .branches(new ArrayList<>())
                .build();

        when(franchiseRepository.findById("franchise-id")).thenReturn(Mono.just(existingFranchise));
        when(franchiseRepository.save(any(Franchise.class))).thenReturn(Mono.just(updatedFranchise));
        when(cachePort.set(anyString(), any(Franchise.class), any(Duration.class)))
                .thenReturn(Mono.just(false));

        Mono<Franchise> result = updateFranchiseNameService.execute("franchise-id", "New Franchise Name");

        StepVerifier.create(result)
                .expectNextMatches(franchise -> franchise.getName().equals("New Franchise Name"))
                .verifyComplete();

        verify(franchiseRepository, times(1)).findById("franchise-id");
        verify(franchiseRepository, times(1)).save(any(Franchise.class));
        verify(cachePort, times(1)).set(anyString(), any(Franchise.class), any(Duration.class));
    }

    @Test
    void execute_ShouldUpdateNameWithBranches() {
        Branch branch = Branch.builder()
                .id("branch-id")
                .name("Test Branch")
                .products(new ArrayList<>())
                .build();
        existingFranchise.getBranches().add(branch);

        Franchise updatedFranchise = Franchise.builder()
                .id("franchise-id")
                .name("Updated Name")
                .branches(new ArrayList<>())
                .build();
        updatedFranchise.getBranches().add(branch);

        when(franchiseRepository.findById("franchise-id")).thenReturn(Mono.just(existingFranchise));
        when(franchiseRepository.save(any(Franchise.class))).thenReturn(Mono.just(updatedFranchise));
        when(cachePort.set(anyString(), any(Franchise.class), any(Duration.class))).thenReturn(Mono.just(true));

        Mono<Franchise> result = updateFranchiseNameService.execute("franchise-id", "Updated Name");

        StepVerifier.create(result)
                .expectNextMatches(franchise ->
                        franchise.getName().equals("Updated Name") &&
                        franchise.getBranches().size() == 1)
                .verifyComplete();

        verify(franchiseRepository, times(1)).findById("franchise-id");
        verify(franchiseRepository, times(1)).save(any(Franchise.class));
    }
}
