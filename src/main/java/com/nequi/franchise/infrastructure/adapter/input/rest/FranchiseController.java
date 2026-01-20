package com.nequi.franchise.infrastructure.adapter.input.rest;

import com.nequi.franchise.application.dto.ApiResponse;
import com.nequi.franchise.application.dto.CreateFranchiseRequest;
import com.nequi.franchise.application.dto.FranchiseResponse;
import com.nequi.franchise.application.mapper.FranchiseResponseMapper;
import com.nequi.franchise.domain.port.input.CreateFranchiseUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/franchises")
@Tag(name = "Franchises", description = "Franchise management endpoints")
public class FranchiseController {

    private final CreateFranchiseUseCase createFranchiseUseCase;
    private final FranchiseResponseMapper franchiseResponseMapper;

    public FranchiseController(
            CreateFranchiseUseCase createFranchiseUseCase,
            FranchiseResponseMapper franchiseResponseMapper) {
        this.createFranchiseUseCase = createFranchiseUseCase;
        this.franchiseResponseMapper = franchiseResponseMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new franchise", description = "Creates a new franchise with the given name")
    public Mono<ApiResponse<FranchiseResponse>> createFranchise(@Valid @RequestBody CreateFranchiseRequest request) {
        return createFranchiseUseCase.execute(request.getName())
                .map(franchiseResponseMapper::toResponse)
                .map(response -> ApiResponse.created(response, "Franchise created successfully"));
    }
}
