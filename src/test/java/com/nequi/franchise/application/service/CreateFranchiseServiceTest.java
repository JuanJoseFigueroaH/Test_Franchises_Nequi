package com.nequi.franchise.application.service;

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
class CreateFranchiseServiceTest {

    @Mock
    private FranchiseRepositoryPort franchiseRepository;

    @Mock
    private CachePort cachePort;

    @InjectMocks
    private CreateFranchiseService createFranchiseService;

    private Franchise expectedFranchise;

    @BeforeEach
    void setUp() {
        expectedFranchise = Franchise.builder()
                .id("test-id")
                .name("Test Franchise")
                .branches(new ArrayList<>())
                .build();
    }

    @Test
    void execute_ShouldCreateFranchiseSuccessfully() {
        when(franchiseRepository.save(any(Franchise.class))).thenReturn(Mono.just(expectedFranchise));
        when(cachePort.set(anyString(), any(Franchise.class), any(Duration.class))).thenReturn(Mono.just(true));

        Mono<Franchise> result = createFranchiseService.execute("Test Franchise");

        StepVerifier.create(result)
                .expectNextMatches(franchise ->
                        franchise.getName().equals("Test Franchise") &&
                        franchise.getBranches().isEmpty())
                .verifyComplete();

        verify(franchiseRepository, times(1)).save(any(Franchise.class));
        verify(cachePort, times(1)).set(anyString(), any(Franchise.class), any(Duration.class));
    }

    @Test
    void execute_ShouldHandleRepositoryError() {
        when(franchiseRepository.save(any(Franchise.class)))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        Mono<Franchise> result = createFranchiseService.execute("Test Franchise");

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(franchiseRepository, times(1)).save(any(Franchise.class));
        verify(cachePort, never()).set(anyString(), any(Franchise.class), any(Duration.class));
    }

    @Test
    void execute_ShouldContinueWhenCacheFails() {
        when(franchiseRepository.save(any(Franchise.class))).thenReturn(Mono.just(expectedFranchise));
        when(cachePort.set(anyString(), any(Franchise.class), any(Duration.class)))
                .thenReturn(Mono.just(false));

        Mono<Franchise> result = createFranchiseService.execute("Test Franchise");

        StepVerifier.create(result)
                .expectNextMatches(franchise -> franchise.getName().equals("Test Franchise"))
                .verifyComplete();

        verify(franchiseRepository, times(1)).save(any(Franchise.class));
        verify(cachePort, times(1)).set(anyString(), any(Franchise.class), any(Duration.class));
    }
}
