package com.nequi.franchise.infrastructure.adapter.input.rest;

import com.nequi.franchise.application.dto.CreateBranchRequest;
import com.nequi.franchise.application.dto.CreateFranchiseRequest;
import com.nequi.franchise.application.dto.FranchiseResponse;
import com.nequi.franchise.application.mapper.FranchiseResponseMapper;
import com.nequi.franchise.domain.exception.FranchiseNotFoundException;
import com.nequi.franchise.domain.model.Branch;
import com.nequi.franchise.domain.model.Franchise;
import com.nequi.franchise.domain.port.input.AddBranchToFranchiseUseCase;
import com.nequi.franchise.domain.port.input.CreateFranchiseUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FranchiseControllerTest {

    @Mock
    private CreateFranchiseUseCase createFranchiseUseCase;

    @Mock
    private AddBranchToFranchiseUseCase addBranchToFranchiseUseCase;

    @Mock
    private FranchiseResponseMapper franchiseResponseMapper;

    @InjectMocks
    private FranchiseController franchiseController;

    private Franchise franchise;
    private FranchiseResponse franchiseResponse;
    private CreateFranchiseRequest request;

    @BeforeEach
    void setUp() {
        franchise = Franchise.builder()
                .id("test-id")
                .name("Test Franchise")
                .branches(new ArrayList<>())
                .build();

        franchiseResponse = FranchiseResponse.builder()
                .id("test-id")
                .name("Test Franchise")
                .branches(new ArrayList<>())
                .build();

        request = CreateFranchiseRequest.builder()
                .name("Test Franchise")
                .build();
    }

    @Test
    void createFranchise_ShouldReturnCreatedResponse() {
        when(createFranchiseUseCase.execute(anyString())).thenReturn(Mono.just(franchise));
        when(franchiseResponseMapper.toResponse(any(Franchise.class))).thenReturn(franchiseResponse);

        var result = franchiseController.createFranchise(request);

        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getStatusCode().equals(201) &&
                        response.getMessage().equals("Franchise created successfully") &&
                        response.getData().getId().equals("test-id"))
                .verifyComplete();

        verify(createFranchiseUseCase, times(1)).execute("Test Franchise");
        verify(franchiseResponseMapper, times(1)).toResponse(franchise);
    }

    @Test
    void createFranchise_ShouldPropagateError() {
        when(createFranchiseUseCase.execute(anyString()))
                .thenReturn(Mono.error(new RuntimeException("Error")));

        var result = franchiseController.createFranchise(request);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(createFranchiseUseCase, times(1)).execute("Test Franchise");
        verify(franchiseResponseMapper, never()).toResponse(any());
    }

    @Test
    void addBranchToFranchise_ShouldReturnCreatedResponse() {
        Branch branch = Branch.builder()
                .id("branch-id")
                .name("Test Branch")
                .products(new ArrayList<>())
                .build();

        Franchise franchiseWithBranch = Franchise.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(List.of(branch))
                .build();

        FranchiseResponse responseWithBranch = FranchiseResponse.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(new ArrayList<>())
                .build();

        CreateBranchRequest branchRequest = CreateBranchRequest.builder()
                .name("Test Branch")
                .build();

        when(addBranchToFranchiseUseCase.execute("franchise-id", "Test Branch"))
                .thenReturn(Mono.just(franchiseWithBranch));
        when(franchiseResponseMapper.toResponse(any(Franchise.class))).thenReturn(responseWithBranch);

        var result = franchiseController.addBranchToFranchise("franchise-id", branchRequest);

        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getStatusCode().equals(201) &&
                        response.getMessage().equals("Branch added successfully to franchise") &&
                        response.getData().getId().equals("franchise-id"))
                .verifyComplete();

        verify(addBranchToFranchiseUseCase, times(1)).execute("franchise-id", "Test Branch");
        verify(franchiseResponseMapper, times(1)).toResponse(franchiseWithBranch);
    }

    @Test
    void addBranchToFranchise_ShouldPropagateErrorWhenFranchiseNotFound() {
        CreateBranchRequest branchRequest = CreateBranchRequest.builder()
                .name("Test Branch")
                .build();

        when(addBranchToFranchiseUseCase.execute("non-existent-id", "Test Branch"))
                .thenReturn(Mono.error(new FranchiseNotFoundException("Franchise not found")));

        var result = franchiseController.addBranchToFranchise("non-existent-id", branchRequest);

        StepVerifier.create(result)
                .expectError(FranchiseNotFoundException.class)
                .verify();

        verify(addBranchToFranchiseUseCase, times(1)).execute("non-existent-id", "Test Branch");
        verify(franchiseResponseMapper, never()).toResponse(any());
    }
}
