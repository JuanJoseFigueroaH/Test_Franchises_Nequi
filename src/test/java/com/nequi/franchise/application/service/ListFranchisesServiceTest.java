package com.nequi.franchise.application.service;

import com.nequi.franchise.domain.model.Franchise;
import com.nequi.franchise.domain.model.Page;
import com.nequi.franchise.domain.port.output.FranchiseRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListFranchisesServiceTest {

    @Mock
    private FranchiseRepositoryPort franchiseRepository;

    private ListFranchisesService listFranchisesService;

    @BeforeEach
    void setUp() {
        listFranchisesService = new ListFranchisesService(franchiseRepository);
    }

    @Test
    void execute_ShouldReturnPageOfFranchises_WhenFranchisesExist() {
        Franchise franchise1 = Franchise.builder()
                .id("franchise-1")
                .name("Franchise 1")
                .build();

        Franchise franchise2 = Franchise.builder()
                .id("franchise-2")
                .name("Franchise 2")
                .build();

        Page<Franchise> page = Page.of(List.of(franchise1, franchise2), "next-cursor", 20);

        when(franchiseRepository.findAll(20, null)).thenReturn(Mono.just(page));

        var result = listFranchisesService.execute(20, null);

        StepVerifier.create(result)
                .expectNextMatches(p -> 
                    p.getItems().size() == 2 &&
                    p.getPageSize() == 2 &&
                    p.getNextCursor().equals("next-cursor") &&
                    p.getHasMore()
                )
                .verifyComplete();

        verify(franchiseRepository, times(1)).findAll(20, null);
    }

    @Test
    void execute_ShouldReturnEmptyPage_WhenNoFranchisesExist() {
        when(franchiseRepository.findAll(20, null)).thenReturn(Mono.just(Page.empty()));

        var result = listFranchisesService.execute(20, null);

        StepVerifier.create(result)
                .expectNextMatches(p -> 
                    p.getItems().isEmpty() &&
                    p.getPageSize() == 0 &&
                    p.getNextCursor() == null &&
                    !p.getHasMore()
                )
                .verifyComplete();
    }

    @Test
    void execute_ShouldUseDefaultPageSize_WhenPageSizeIsNull() {
        Page<Franchise> page = Page.empty();
        when(franchiseRepository.findAll(20, null)).thenReturn(Mono.just(page));

        var result = listFranchisesService.execute(null, null);

        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();

        verify(franchiseRepository, times(1)).findAll(20, null);
    }

    @Test
    void execute_ShouldLimitPageSize_WhenPageSizeExceedsMaximum() {
        Page<Franchise> page = Page.empty();
        when(franchiseRepository.findAll(100, null)).thenReturn(Mono.just(page));

        var result = listFranchisesService.execute(200, null);

        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();

        verify(franchiseRepository, times(1)).findAll(100, null);
    }

    @Test
    void execute_ShouldUseCursor_WhenCursorProvided() {
        Page<Franchise> page = Page.empty();
        when(franchiseRepository.findAll(20, "cursor-123")).thenReturn(Mono.just(page));

        var result = listFranchisesService.execute(20, "cursor-123");

        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();

        verify(franchiseRepository, times(1)).findAll(20, "cursor-123");
    }

    @Test
    void execute_ShouldPropagateError_WhenRepositoryFails() {
        when(franchiseRepository.findAll(anyInt(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        var result = listFranchisesService.execute(20, null);

        StepVerifier.create(result)
                .expectErrorMatches(error -> 
                    error instanceof RuntimeException &&
                    error.getMessage().equals("Database error")
                )
                .verify();
    }

    @Test
    void execute_ShouldReturnLastPage_WhenNoNextCursor() {
        Franchise franchise = Franchise.builder()
                .id("franchise-1")
                .name("Last Franchise")
                .build();

        Page<Franchise> page = Page.of(List.of(franchise), null, 20);

        when(franchiseRepository.findAll(20, null)).thenReturn(Mono.just(page));

        var result = listFranchisesService.execute(20, null);

        StepVerifier.create(result)
                .expectNextMatches(p -> 
                    p.getItems().size() == 1 &&
                    p.getNextCursor() == null &&
                    !p.getHasMore()
                )
                .verifyComplete();
    }
}
