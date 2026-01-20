package com.nequi.franchise.infrastructure.adapter.input.rest;

import com.nequi.franchise.application.dto.ApiResponse;
import com.nequi.franchise.application.dto.CreateBranchRequest;
import com.nequi.franchise.application.dto.CreateFranchiseRequest;
import com.nequi.franchise.application.dto.CreateProductRequest;
import com.nequi.franchise.application.dto.FranchiseResponse;
import com.nequi.franchise.application.mapper.FranchiseResponseMapper;
import com.nequi.franchise.domain.port.input.AddBranchToFranchiseUseCase;
import com.nequi.franchise.domain.port.input.AddProductToBranchUseCase;
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
    private final AddBranchToFranchiseUseCase addBranchToFranchiseUseCase;
    private final AddProductToBranchUseCase addProductToBranchUseCase;
    private final FranchiseResponseMapper franchiseResponseMapper;

    public FranchiseController(
            CreateFranchiseUseCase createFranchiseUseCase,
            AddBranchToFranchiseUseCase addBranchToFranchiseUseCase,
            AddProductToBranchUseCase addProductToBranchUseCase,
            FranchiseResponseMapper franchiseResponseMapper) {
        this.createFranchiseUseCase = createFranchiseUseCase;
        this.addBranchToFranchiseUseCase = addBranchToFranchiseUseCase;
        this.addProductToBranchUseCase = addProductToBranchUseCase;
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

    @PostMapping("/{franchiseId}/branches")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a new branch to a franchise", description = "Adds a new branch to an existing franchise")
    public Mono<ApiResponse<FranchiseResponse>> addBranchToFranchise(
            @PathVariable String franchiseId,
            @Valid @RequestBody CreateBranchRequest request) {
        return addBranchToFranchiseUseCase.execute(franchiseId, request.getName())
                .map(franchiseResponseMapper::toResponse)
                .map(response -> ApiResponse.created(response, "Branch added successfully to franchise"));
    }

    @PostMapping("/{franchiseId}/branches/{branchId}/products")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a new product to a branch", description = "Adds a new product to an existing branch")
    public Mono<ApiResponse<FranchiseResponse>> addProductToBranch(
            @PathVariable String franchiseId,
            @PathVariable String branchId,
            @Valid @RequestBody CreateProductRequest request) {
        return addProductToBranchUseCase.execute(franchiseId, branchId, request.getName(), request.getStock())
                .map(franchiseResponseMapper::toResponse)
                .map(response -> ApiResponse.created(response, "Product added successfully to branch"));
    }
}
