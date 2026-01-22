package com.nequi.franchise.infrastructure.adapter.input.rest;

import com.nequi.franchise.application.dto.CreateBranchRequest;
import com.nequi.franchise.application.dto.CreateFranchiseRequest;
import com.nequi.franchise.application.dto.CreateProductRequest;
import com.nequi.franchise.application.dto.FranchiseResponse;
import com.nequi.franchise.application.dto.UpdateNameRequest;
import com.nequi.franchise.application.dto.UpdateStockRequest;
import com.nequi.franchise.application.mapper.FranchiseResponseMapper;
import com.nequi.franchise.domain.exception.BranchNotFoundException;
import com.nequi.franchise.domain.exception.FranchiseNotFoundException;
import com.nequi.franchise.domain.exception.ProductNotFoundException;
import com.nequi.franchise.domain.model.Branch;
import com.nequi.franchise.domain.model.Franchise;
import com.nequi.franchise.domain.model.Product;
import com.nequi.franchise.domain.port.input.AddBranchToFranchiseUseCase;
import com.nequi.franchise.domain.port.input.AddProductToBranchUseCase;
import com.nequi.franchise.domain.port.input.CreateFranchiseUseCase;
import com.nequi.franchise.domain.port.input.DeleteProductFromBranchUseCase;
import com.nequi.franchise.domain.port.input.GetMaxStockProductsUseCase;
import com.nequi.franchise.domain.port.input.UpdateBranchNameUseCase;
import com.nequi.franchise.domain.port.input.UpdateFranchiseNameUseCase;
import com.nequi.franchise.domain.port.input.UpdateProductNameUseCase;
import com.nequi.franchise.domain.port.input.UpdateProductStockUseCase;
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
    private AddProductToBranchUseCase addProductToBranchUseCase;

    @Mock
    private DeleteProductFromBranchUseCase deleteProductFromBranchUseCase;

    @Mock
    private UpdateProductStockUseCase updateProductStockUseCase;

    @Mock
    private GetMaxStockProductsUseCase getMaxStockProductsUseCase;

    @Mock
    private UpdateFranchiseNameUseCase updateFranchiseNameUseCase;

    @Mock
    private UpdateBranchNameUseCase updateBranchNameUseCase;

    @Mock
    private UpdateProductNameUseCase updateProductNameUseCase;

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
                .franchiseId("franchise-id")
                .name("Test Branch")
                .build();

        when(addBranchToFranchiseUseCase.execute("franchise-id", "Test Branch"))
                .thenReturn(Mono.just(franchiseWithBranch));
        when(franchiseResponseMapper.toResponse(any(Franchise.class))).thenReturn(responseWithBranch);

        var result = franchiseController.addBranchToFranchise(branchRequest);

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
                .franchiseId("non-existent-id")
                .name("Test Branch")
                .build();

        when(addBranchToFranchiseUseCase.execute("non-existent-id", "Test Branch"))
                .thenReturn(Mono.error(new FranchiseNotFoundException("Franchise not found")));

        var result = franchiseController.addBranchToFranchise(branchRequest);

        StepVerifier.create(result)
                .expectError(FranchiseNotFoundException.class)
                .verify();

        verify(addBranchToFranchiseUseCase, times(1)).execute("non-existent-id", "Test Branch");
        verify(franchiseResponseMapper, never()).toResponse(any());
    }

    @Test
    void addProductToBranch_ShouldReturnCreatedResponse() {
        Product product = Product.builder()
                .id("product-id")
                .name("Test Product")
                .stock(100)
                .build();

        Branch branch = Branch.builder()
                .id("branch-id")
                .name("Test Branch")
                .products(List.of(product))
                .build();

        Franchise franchiseWithProduct = Franchise.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(List.of(branch))
                .build();

        FranchiseResponse responseWithProduct = FranchiseResponse.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(new ArrayList<>())
                .build();

        CreateProductRequest productRequest = CreateProductRequest.builder()
                .franchiseId("franchise-id")
                .branchId("branch-id")
                .name("Test Product")
                .stock(100)
                .build();

        when(addProductToBranchUseCase.execute("franchise-id", "branch-id", "Test Product", 100))
                .thenReturn(Mono.just(franchiseWithProduct));
        when(franchiseResponseMapper.toResponse(any(Franchise.class))).thenReturn(responseWithProduct);

        var result = franchiseController.addProductToBranch(productRequest);

        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getStatusCode().equals(201) &&
                        response.getMessage().equals("Product added successfully to branch") &&
                        response.getData().getId().equals("franchise-id"))
                .verifyComplete();

        verify(addProductToBranchUseCase, times(1)).execute("franchise-id", "branch-id", "Test Product", 100);
        verify(franchiseResponseMapper, times(1)).toResponse(franchiseWithProduct);
    }

    @Test
    void addProductToBranch_ShouldPropagateErrorWhenFranchiseNotFound() {
        CreateProductRequest productRequest = CreateProductRequest.builder()
                .franchiseId("non-existent-id")
                .branchId("branch-id")
                .name("Test Product")
                .stock(100)
                .build();

        when(addProductToBranchUseCase.execute("non-existent-id", "branch-id", "Test Product", 100))
                .thenReturn(Mono.error(new FranchiseNotFoundException("Franchise not found")));

        var result = franchiseController.addProductToBranch(productRequest);

        StepVerifier.create(result)
                .expectError(FranchiseNotFoundException.class)
                .verify();

        verify(addProductToBranchUseCase, times(1)).execute("non-existent-id", "branch-id", "Test Product", 100);
        verify(franchiseResponseMapper, never()).toResponse(any());
    }

    @Test
    void addProductToBranch_ShouldPropagateErrorWhenBranchNotFound() {
        CreateProductRequest productRequest = CreateProductRequest.builder()
                .franchiseId("franchise-id")
                .branchId("non-existent-branch")
                .name("Test Product")
                .stock(100)
                .build();

        when(addProductToBranchUseCase.execute("franchise-id", "non-existent-branch", "Test Product", 100))
                .thenReturn(Mono.error(new BranchNotFoundException("Branch not found")));

        var result = franchiseController.addProductToBranch(productRequest);

        StepVerifier.create(result)
                .expectError(BranchNotFoundException.class)
                .verify();

        verify(addProductToBranchUseCase, times(1)).execute("franchise-id", "non-existent-branch", "Test Product", 100);
        verify(franchiseResponseMapper, never()).toResponse(any());
    }

    @Test
    void deleteProductFromBranch_ShouldReturnSuccessResponse() {
        Branch branch = Branch.builder()
                .id("branch-id")
                .name("Test Branch")
                .products(new ArrayList<>())
                .build();

        Franchise franchiseWithoutProduct = Franchise.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(List.of(branch))
                .build();

        FranchiseResponse responseWithoutProduct = FranchiseResponse.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(new ArrayList<>())
                .build();

        when(deleteProductFromBranchUseCase.execute("franchise-id", "branch-id", "product-id"))
                .thenReturn(Mono.just(franchiseWithoutProduct));
        when(franchiseResponseMapper.toResponse(any(Franchise.class))).thenReturn(responseWithoutProduct);

        var result = franchiseController.deleteProductFromBranch("franchise-id", "branch-id", "product-id");

        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getStatusCode().equals(200) &&
                        response.getMessage().equals("Product deleted successfully from branch") &&
                        response.getData().getId().equals("franchise-id"))
                .verifyComplete();

        verify(deleteProductFromBranchUseCase, times(1)).execute("franchise-id", "branch-id", "product-id");
        verify(franchiseResponseMapper, times(1)).toResponse(franchiseWithoutProduct);
    }

    @Test
    void deleteProductFromBranch_ShouldPropagateErrorWhenFranchiseNotFound() {
        when(deleteProductFromBranchUseCase.execute("non-existent-id", "branch-id", "product-id"))
                .thenReturn(Mono.error(new FranchiseNotFoundException("Franchise not found")));

        var result = franchiseController.deleteProductFromBranch("non-existent-id", "branch-id", "product-id");

        StepVerifier.create(result)
                .expectError(FranchiseNotFoundException.class)
                .verify();

        verify(deleteProductFromBranchUseCase, times(1)).execute("non-existent-id", "branch-id", "product-id");
        verify(franchiseResponseMapper, never()).toResponse(any());
    }

    @Test
    void deleteProductFromBranch_ShouldPropagateErrorWhenBranchNotFound() {
        when(deleteProductFromBranchUseCase.execute("franchise-id", "non-existent-branch", "product-id"))
                .thenReturn(Mono.error(new BranchNotFoundException("Branch not found")));

        var result = franchiseController.deleteProductFromBranch("franchise-id", "non-existent-branch", "product-id");

        StepVerifier.create(result)
                .expectError(BranchNotFoundException.class)
                .verify();

        verify(deleteProductFromBranchUseCase, times(1)).execute("franchise-id", "non-existent-branch", "product-id");
        verify(franchiseResponseMapper, never()).toResponse(any());
    }

    @Test
    void deleteProductFromBranch_ShouldPropagateErrorWhenProductNotFound() {
        when(deleteProductFromBranchUseCase.execute("franchise-id", "branch-id", "non-existent-product"))
                .thenReturn(Mono.error(new ProductNotFoundException("Product not found")));

        var result = franchiseController.deleteProductFromBranch("franchise-id", "branch-id", "non-existent-product");

        StepVerifier.create(result)
                .expectError(ProductNotFoundException.class)
                .verify();

        verify(deleteProductFromBranchUseCase, times(1)).execute("franchise-id", "branch-id", "non-existent-product");
        verify(franchiseResponseMapper, never()).toResponse(any());
    }

    @Test
    void updateProductStock_ShouldReturnSuccessResponse() {
        Product product = Product.builder()
                .id("product-id")
                .name("Test Product")
                .stock(200)
                .build();

        Branch branch = Branch.builder()
                .id("branch-id")
                .name("Test Branch")
                .products(List.of(product))
                .build();

        Franchise franchiseWithUpdatedStock = Franchise.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(List.of(branch))
                .build();

        FranchiseResponse responseWithUpdatedStock = FranchiseResponse.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(new ArrayList<>())
                .build();

        UpdateStockRequest stockRequest = UpdateStockRequest.builder()
                .stock(200)
                .build();

        when(updateProductStockUseCase.execute("franchise-id", "branch-id", "product-id", 200))
                .thenReturn(Mono.just(franchiseWithUpdatedStock));
        when(franchiseResponseMapper.toResponse(any(Franchise.class))).thenReturn(responseWithUpdatedStock);

        var result = franchiseController.updateProductStock("franchise-id", "branch-id", "product-id", stockRequest);

        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getStatusCode().equals(200) &&
                        response.getMessage().equals("Product stock updated successfully") &&
                        response.getData().getId().equals("franchise-id"))
                .verifyComplete();

        verify(updateProductStockUseCase, times(1)).execute("franchise-id", "branch-id", "product-id", 200);
        verify(franchiseResponseMapper, times(1)).toResponse(franchiseWithUpdatedStock);
    }

    @Test
    void updateProductStock_ShouldPropagateErrorWhenFranchiseNotFound() {
        UpdateStockRequest stockRequest = UpdateStockRequest.builder()
                .stock(200)
                .build();

        when(updateProductStockUseCase.execute("non-existent-id", "branch-id", "product-id", 200))
                .thenReturn(Mono.error(new FranchiseNotFoundException("Franchise not found")));

        var result = franchiseController.updateProductStock("non-existent-id", "branch-id", "product-id", stockRequest);

        StepVerifier.create(result)
                .expectError(FranchiseNotFoundException.class)
                .verify();

        verify(updateProductStockUseCase, times(1)).execute("non-existent-id", "branch-id", "product-id", 200);
        verify(franchiseResponseMapper, never()).toResponse(any());
    }

    @Test
    void updateProductStock_ShouldPropagateErrorWhenBranchNotFound() {
        UpdateStockRequest stockRequest = UpdateStockRequest.builder()
                .stock(200)
                .build();

        when(updateProductStockUseCase.execute("franchise-id", "non-existent-branch", "product-id", 200))
                .thenReturn(Mono.error(new BranchNotFoundException("Branch not found")));

        var result = franchiseController.updateProductStock("franchise-id", "non-existent-branch", "product-id", stockRequest);

        StepVerifier.create(result)
                .expectError(BranchNotFoundException.class)
                .verify();

        verify(updateProductStockUseCase, times(1)).execute("franchise-id", "non-existent-branch", "product-id", 200);
        verify(franchiseResponseMapper, never()).toResponse(any());
    }

    @Test
    void updateProductStock_ShouldPropagateErrorWhenProductNotFound() {
        UpdateStockRequest stockRequest = UpdateStockRequest.builder()
                .stock(200)
                .build();

        when(updateProductStockUseCase.execute("franchise-id", "branch-id", "non-existent-product", 200))
                .thenReturn(Mono.error(new ProductNotFoundException("Product not found")));

        var result = franchiseController.updateProductStock("franchise-id", "branch-id", "non-existent-product", stockRequest);

        StepVerifier.create(result)
                .expectError(ProductNotFoundException.class)
                .verify();

        verify(updateProductStockUseCase, times(1)).execute("franchise-id", "branch-id", "non-existent-product", 200);
        verify(franchiseResponseMapper, never()).toResponse(any());
    }

    @Test
    void getMaxStockProducts_ShouldReturnSuccessResponse() {
        Product maxProduct1 = Product.builder()
                .id("product-1")
                .name("Max Product 1")
                .stock(100)
                .build();

        Product maxProduct2 = Product.builder()
                .id("product-2")
                .name("Max Product 2")
                .stock(150)
                .build();

        Branch branch1 = Branch.builder()
                .id("branch-1")
                .name("Branch 1")
                .products(List.of(maxProduct1))
                .build();

        Branch branch2 = Branch.builder()
                .id("branch-2")
                .name("Branch 2")
                .products(List.of(maxProduct2))
                .build();

        Franchise franchiseWithMaxProducts = Franchise.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(List.of(branch1, branch2))
                .build();

        FranchiseResponse responseWithMaxProducts = FranchiseResponse.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(new ArrayList<>())
                .build();

        when(getMaxStockProductsUseCase.execute("franchise-id"))
                .thenReturn(Mono.just(franchiseWithMaxProducts));
        when(franchiseResponseMapper.toResponse(any(Franchise.class))).thenReturn(responseWithMaxProducts);

        var result = franchiseController.getMaxStockProducts("franchise-id");

        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getStatusCode().equals(200) &&
                        response.getMessage().equals("Max stock products retrieved successfully") &&
                        response.getData().getId().equals("franchise-id"))
                .verifyComplete();

        verify(getMaxStockProductsUseCase, times(1)).execute("franchise-id");
        verify(franchiseResponseMapper, times(1)).toResponse(franchiseWithMaxProducts);
    }

    @Test
    void getMaxStockProducts_ShouldPropagateErrorWhenFranchiseNotFound() {
        when(getMaxStockProductsUseCase.execute("non-existent-id"))
                .thenReturn(Mono.error(new FranchiseNotFoundException("Franchise not found")));

        var result = franchiseController.getMaxStockProducts("non-existent-id");

        StepVerifier.create(result)
                .expectError(FranchiseNotFoundException.class)
                .verify();

        verify(getMaxStockProductsUseCase, times(1)).execute("non-existent-id");
        verify(franchiseResponseMapper, never()).toResponse(any());
    }

    @Test
    void updateFranchiseName_ShouldReturnSuccessResponse() {
        Franchise updatedFranchise = Franchise.builder()
                .id("franchise-id")
                .name("Updated Franchise Name")
                .branches(new ArrayList<>())
                .build();

        FranchiseResponse response = FranchiseResponse.builder()
                .id("franchise-id")
                .name("Updated Franchise Name")
                .branches(new ArrayList<>())
                .build();

        UpdateNameRequest nameRequest = UpdateNameRequest.builder()
                .name("Updated Franchise Name")
                .build();

        when(updateFranchiseNameUseCase.execute("franchise-id", "Updated Franchise Name"))
                .thenReturn(Mono.just(updatedFranchise));
        when(franchiseResponseMapper.toResponse(any(Franchise.class))).thenReturn(response);

        var result = franchiseController.updateFranchiseName("franchise-id", nameRequest);

        StepVerifier.create(result)
                .expectNextMatches(r ->
                        r.getStatusCode().equals(200) &&
                        r.getMessage().equals("Franchise name updated successfully") &&
                        r.getData().getName().equals("Updated Franchise Name"))
                .verifyComplete();

        verify(updateFranchiseNameUseCase, times(1)).execute("franchise-id", "Updated Franchise Name");
        verify(franchiseResponseMapper, times(1)).toResponse(updatedFranchise);
    }

    @Test
    void updateFranchiseName_ShouldPropagateErrorWhenFranchiseNotFound() {
        UpdateNameRequest nameRequest = UpdateNameRequest.builder()
                .name("New Name")
                .build();

        when(updateFranchiseNameUseCase.execute("non-existent-id", "New Name"))
                .thenReturn(Mono.error(new FranchiseNotFoundException("Franchise not found")));

        var result = franchiseController.updateFranchiseName("non-existent-id", nameRequest);

        StepVerifier.create(result)
                .expectError(FranchiseNotFoundException.class)
                .verify();

        verify(updateFranchiseNameUseCase, times(1)).execute("non-existent-id", "New Name");
        verify(franchiseResponseMapper, never()).toResponse(any());
    }

    @Test
    void updateBranchName_ShouldReturnSuccessResponse() {
        Branch updatedBranch = Branch.builder()
                .id("branch-id")
                .name("Updated Branch Name")
                .products(new ArrayList<>())
                .build();

        Franchise updatedFranchise = Franchise.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(List.of(updatedBranch))
                .build();

        FranchiseResponse response = FranchiseResponse.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(new ArrayList<>())
                .build();

        UpdateNameRequest nameRequest = UpdateNameRequest.builder()
                .name("Updated Branch Name")
                .build();

        when(updateBranchNameUseCase.execute("franchise-id", "branch-id", "Updated Branch Name"))
                .thenReturn(Mono.just(updatedFranchise));
        when(franchiseResponseMapper.toResponse(any(Franchise.class))).thenReturn(response);

        var result = franchiseController.updateBranchName("franchise-id", "branch-id", nameRequest);

        StepVerifier.create(result)
                .expectNextMatches(r ->
                        r.getStatusCode().equals(200) &&
                        r.getMessage().equals("Branch name updated successfully") &&
                        r.getData().getId().equals("franchise-id"))
                .verifyComplete();

        verify(updateBranchNameUseCase, times(1)).execute("franchise-id", "branch-id", "Updated Branch Name");
        verify(franchiseResponseMapper, times(1)).toResponse(updatedFranchise);
    }

    @Test
    void updateBranchName_ShouldPropagateErrorWhenBranchNotFound() {
        UpdateNameRequest nameRequest = UpdateNameRequest.builder()
                .name("New Name")
                .build();

        when(updateBranchNameUseCase.execute("franchise-id", "non-existent-branch", "New Name"))
                .thenReturn(Mono.error(new BranchNotFoundException("Branch not found")));

        var result = franchiseController.updateBranchName("franchise-id", "non-existent-branch", nameRequest);

        StepVerifier.create(result)
                .expectError(BranchNotFoundException.class)
                .verify();

        verify(updateBranchNameUseCase, times(1)).execute("franchise-id", "non-existent-branch", "New Name");
        verify(franchiseResponseMapper, never()).toResponse(any());
    }

    @Test
    void updateProductName_ShouldReturnSuccessResponse() {
        Product updatedProduct = Product.builder()
                .id("product-id")
                .name("Updated Product Name")
                .stock(100)
                .build();

        Branch branch = Branch.builder()
                .id("branch-id")
                .name("Test Branch")
                .products(List.of(updatedProduct))
                .build();

        Franchise updatedFranchise = Franchise.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(List.of(branch))
                .build();

        FranchiseResponse response = FranchiseResponse.builder()
                .id("franchise-id")
                .name("Test Franchise")
                .branches(new ArrayList<>())
                .build();

        UpdateNameRequest nameRequest = UpdateNameRequest.builder()
                .name("Updated Product Name")
                .build();

        when(updateProductNameUseCase.execute("franchise-id", "branch-id", "product-id", "Updated Product Name"))
                .thenReturn(Mono.just(updatedFranchise));
        when(franchiseResponseMapper.toResponse(any(Franchise.class))).thenReturn(response);

        var result = franchiseController.updateProductName("franchise-id", "branch-id", "product-id", nameRequest);

        StepVerifier.create(result)
                .expectNextMatches(r ->
                        r.getStatusCode().equals(200) &&
                        r.getMessage().equals("Product name updated successfully") &&
                        r.getData().getId().equals("franchise-id"))
                .verifyComplete();

        verify(updateProductNameUseCase, times(1)).execute("franchise-id", "branch-id", "product-id", "Updated Product Name");
        verify(franchiseResponseMapper, times(1)).toResponse(updatedFranchise);
    }

    @Test
    void updateProductName_ShouldPropagateErrorWhenProductNotFound() {
        UpdateNameRequest nameRequest = UpdateNameRequest.builder()
                .name("New Name")
                .build();

        when(updateProductNameUseCase.execute("franchise-id", "branch-id", "non-existent-product", "New Name"))
                .thenReturn(Mono.error(new ProductNotFoundException("Product not found")));

        var result = franchiseController.updateProductName("franchise-id", "branch-id", "non-existent-product", nameRequest);

        StepVerifier.create(result)
                .expectError(ProductNotFoundException.class)
                .verify();

        verify(updateProductNameUseCase, times(1)).execute("franchise-id", "branch-id", "non-existent-product", "New Name");
        verify(franchiseResponseMapper, never()).toResponse(any());
    }
}
