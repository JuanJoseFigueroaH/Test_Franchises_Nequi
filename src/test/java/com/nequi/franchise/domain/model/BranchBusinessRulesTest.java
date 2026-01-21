package com.nequi.franchise.domain.model;

import com.nequi.franchise.domain.exception.DuplicateEntityException;
import com.nequi.franchise.domain.exception.InvalidDomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BranchBusinessRulesTest {

    @Test
    void addProduct_ShouldThrowException_WhenDuplicateName() {
        Branch branch = Branch.builder()
                .id("branch-1")
                .name("Test Branch")
                .build();

        Product product1 = Product.builder()
                .id("product-1")
                .name("Product A")
                .stock(100)
                .build();

        Product product2 = Product.builder()
                .id("product-2")
                .name("Product A")
                .stock(50)
                .build();

        branch.addProduct(product1);

        DuplicateEntityException exception = assertThrows(
                DuplicateEntityException.class,
                () -> branch.addProduct(product2)
        );

        assertTrue(exception.getMessage().contains("already exists"));
        assertTrue(exception.getMessage().contains("Product A"));
    }

    @Test
    void addProduct_ShouldThrowException_WhenDuplicateNameCaseInsensitive() {
        Branch branch = Branch.builder()
                .id("branch-1")
                .name("Test Branch")
                .build();

        Product product1 = Product.builder()
                .id("product-1")
                .name("Product A")
                .stock(100)
                .build();

        Product product2 = Product.builder()
                .id("product-2")
                .name("PRODUCT A")
                .stock(50)
                .build();

        branch.addProduct(product1);

        DuplicateEntityException exception = assertThrows(
                DuplicateEntityException.class,
                () -> branch.addProduct(product2)
        );

        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    void addProduct_ShouldSucceed_WhenDifferentNames() {
        Branch branch = Branch.builder()
                .id("branch-1")
                .name("Test Branch")
                .build();

        Product product1 = Product.builder()
                .id("product-1")
                .name("Product A")
                .stock(100)
                .build();

        Product product2 = Product.builder()
                .id("product-2")
                .name("Product B")
                .stock(50)
                .build();

        assertDoesNotThrow(() -> {
            branch.addProduct(product1);
            branch.addProduct(product2);
        });

        assertEquals(2, branch.getProductCount());
    }

    @Test
    void addProduct_ShouldThrowException_WhenExceedsMaxProducts() {
        Branch branch = Branch.builder()
                .id("branch-1")
                .name("Test Branch")
                .build();

        for (int i = 0; i < 1000; i++) {
            Product product = Product.builder()
                    .id("product-" + i)
                    .name("Product " + i)
                    .stock(10)
                    .build();
            branch.addProduct(product);
        }

        Product extraProduct = Product.builder()
                .id("product-1001")
                .name("Extra Product")
                .stock(10)
                .build();

        InvalidDomainException exception = assertThrows(
                InvalidDomainException.class,
                () -> branch.addProduct(extraProduct)
        );

        assertTrue(exception.getMessage().contains("cannot have more than"));
        assertTrue(exception.getMessage().contains("1000"));
    }

    @Test
    void hasProductWithName_ShouldReturnTrue_WhenNameExists() {
        Branch branch = Branch.builder()
                .id("branch-1")
                .name("Test Branch")
                .build();

        Product product = Product.builder()
                .id("product-1")
                .name("Test Product")
                .stock(100)
                .build();

        branch.addProduct(product);

        assertTrue(branch.hasProductWithName("Test Product"));
        assertTrue(branch.hasProductWithName("test product"));
        assertTrue(branch.hasProductWithName("TEST PRODUCT"));
    }

    @Test
    void hasProductWithName_ShouldReturnFalse_WhenNameDoesNotExist() {
        Branch branch = Branch.builder()
                .id("branch-1")
                .name("Test Branch")
                .build();

        assertFalse(branch.hasProductWithName("Non-existent Product"));
    }
}
