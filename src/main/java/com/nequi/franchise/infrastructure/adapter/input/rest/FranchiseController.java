package com.nequi.franchise.infrastructure.adapter.input.rest;

import com.nequi.franchise.application.dto.ApiResponse;
import com.nequi.franchise.application.dto.CreateBranchRequest;
import com.nequi.franchise.application.dto.CreateFranchiseRequest;
import com.nequi.franchise.application.dto.CreateProductRequest;
import com.nequi.franchise.application.dto.FranchiseResponse;
import com.nequi.franchise.application.dto.UpdateNameRequest;
import com.nequi.franchise.application.dto.UpdateStockRequest;
import com.nequi.franchise.application.mapper.FranchiseResponseMapper;
import com.nequi.franchise.domain.port.input.AddBranchToFranchiseUseCase;
import com.nequi.franchise.domain.port.input.AddProductToBranchUseCase;
import com.nequi.franchise.domain.port.input.CreateFranchiseUseCase;
import com.nequi.franchise.domain.port.input.DeleteProductFromBranchUseCase;
import com.nequi.franchise.domain.port.input.GetMaxStockProductsUseCase;
import com.nequi.franchise.domain.port.input.UpdateBranchNameUseCase;
import com.nequi.franchise.domain.port.input.UpdateFranchiseNameUseCase;
import com.nequi.franchise.domain.port.input.UpdateProductNameUseCase;
import com.nequi.franchise.domain.port.input.UpdateProductStockUseCase;
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
    private final DeleteProductFromBranchUseCase deleteProductFromBranchUseCase;
    private final UpdateProductStockUseCase updateProductStockUseCase;
    private final GetMaxStockProductsUseCase getMaxStockProductsUseCase;
    private final UpdateFranchiseNameUseCase updateFranchiseNameUseCase;
    private final UpdateBranchNameUseCase updateBranchNameUseCase;
    private final UpdateProductNameUseCase updateProductNameUseCase;
    private final FranchiseResponseMapper franchiseResponseMapper;

    public FranchiseController(
            CreateFranchiseUseCase createFranchiseUseCase,
            AddBranchToFranchiseUseCase addBranchToFranchiseUseCase,
            AddProductToBranchUseCase addProductToBranchUseCase,
            DeleteProductFromBranchUseCase deleteProductFromBranchUseCase,
            UpdateProductStockUseCase updateProductStockUseCase,
            GetMaxStockProductsUseCase getMaxStockProductsUseCase,
            UpdateFranchiseNameUseCase updateFranchiseNameUseCase,
            UpdateBranchNameUseCase updateBranchNameUseCase,
            UpdateProductNameUseCase updateProductNameUseCase,
            FranchiseResponseMapper franchiseResponseMapper) {
        this.createFranchiseUseCase = createFranchiseUseCase;
        this.addBranchToFranchiseUseCase = addBranchToFranchiseUseCase;
        this.addProductToBranchUseCase = addProductToBranchUseCase;
        this.deleteProductFromBranchUseCase = deleteProductFromBranchUseCase;
        this.updateProductStockUseCase = updateProductStockUseCase;
        this.getMaxStockProductsUseCase = getMaxStockProductsUseCase;
        this.updateFranchiseNameUseCase = updateFranchiseNameUseCase;
        this.updateBranchNameUseCase = updateBranchNameUseCase;
        this.updateProductNameUseCase = updateProductNameUseCase;
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

    @DeleteMapping("/{franchiseId}/branches/{branchId}/products/{productId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Delete a product from a branch", description = "Removes a product from an existing branch")
    public Mono<ApiResponse<FranchiseResponse>> deleteProductFromBranch(
            @PathVariable String franchiseId,
            @PathVariable String branchId,
            @PathVariable String productId) {
        return deleteProductFromBranchUseCase.execute(franchiseId, branchId, productId)
                .map(franchiseResponseMapper::toResponse)
                .map(response -> ApiResponse.success(response, "Product deleted successfully from branch"));
    }

    @PatchMapping("/{franchiseId}/branches/{branchId}/products/{productId}/stock")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update product stock", description = "Updates the stock quantity of a product in a branch")
    public Mono<ApiResponse<FranchiseResponse>> updateProductStock(
            @PathVariable String franchiseId,
            @PathVariable String branchId,
            @PathVariable String productId,
            @Valid @RequestBody UpdateStockRequest request) {
        return updateProductStockUseCase.execute(franchiseId, branchId, productId, request.getStock())
                .map(franchiseResponseMapper::toResponse)
                .map(response -> ApiResponse.success(response, "Product stock updated successfully"));
    }

    @GetMapping("/{franchiseId}/max-stock-products")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get products with maximum stock per branch", description = "Returns the product with the highest stock for each branch in the franchise")
    public Mono<ApiResponse<FranchiseResponse>> getMaxStockProducts(@PathVariable String franchiseId) {
        return getMaxStockProductsUseCase.execute(franchiseId)
                .map(franchiseResponseMapper::toResponse)
                .map(response -> ApiResponse.success(response, "Max stock products retrieved successfully"));
    }

    @PatchMapping("/{franchiseId}/name")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update franchise name", description = "Updates the name of a franchise")
    public Mono<ApiResponse<FranchiseResponse>> updateFranchiseName(
            @PathVariable String franchiseId,
            @Valid @RequestBody UpdateNameRequest request) {
        return updateFranchiseNameUseCase.execute(franchiseId, request.getName())
                .map(franchiseResponseMapper::toResponse)
                .map(response -> ApiResponse.success(response, "Franchise name updated successfully"));
    }

    @PatchMapping("/{franchiseId}/branches/{branchId}/name")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update branch name", description = "Updates the name of a branch")
    public Mono<ApiResponse<FranchiseResponse>> updateBranchName(
            @PathVariable String franchiseId,
            @PathVariable String branchId,
            @Valid @RequestBody UpdateNameRequest request) {
        return updateBranchNameUseCase.execute(franchiseId, branchId, request.getName())
                .map(franchiseResponseMapper::toResponse)
                .map(response -> ApiResponse.success(response, "Branch name updated successfully"));
    }

    @PatchMapping("/{franchiseId}/branches/{branchId}/products/{productId}/name")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update product name", description = "Updates the name of a product")
    public Mono<ApiResponse<FranchiseResponse>> updateProductName(
            @PathVariable String franchiseId,
            @PathVariable String branchId,
            @PathVariable String productId,
            @Valid @RequestBody UpdateNameRequest request) {
        return updateProductNameUseCase.execute(franchiseId, branchId, productId, request.getName())
                .map(franchiseResponseMapper::toResponse)
                .map(response -> ApiResponse.success(response, "Product name updated successfully"));
    }
}
