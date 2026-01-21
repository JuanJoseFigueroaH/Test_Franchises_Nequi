package com.nequi.franchise.domain.model;

import com.nequi.franchise.domain.exception.InvalidDomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductBusinessRulesTest {

    @Test
    void builder_ShouldThrowException_WhenStockIsNegative() {
        InvalidDomainException exception = assertThrows(
                InvalidDomainException.class,
                () -> Product.builder()
                        .id("product-1")
                        .name("Test Product")
                        .stock(-1)
                        .build()
        );

        assertTrue(exception.getMessage().contains("must be between"));
    }

    @Test
    void builder_ShouldThrowException_WhenStockExceedsMaximum() {
        InvalidDomainException exception = assertThrows(
                InvalidDomainException.class,
                () -> Product.builder()
                        .id("product-1")
                        .name("Test Product")
                        .stock(1_000_001)
                        .build()
        );

        assertTrue(exception.getMessage().contains("must be between"));
    }

    @Test
    void builder_ShouldSucceed_WhenStockIsZero() {
        Product product = Product.builder()
                .id("product-1")
                .name("Test Product")
                .stock(0)
                .build();

        assertEquals(0, product.getStock());
    }

    @Test
    void builder_ShouldSucceed_WhenStockIsMaximum() {
        Product product = Product.builder()
                .id("product-1")
                .name("Test Product")
                .stock(1_000_000)
                .build();

        assertEquals(1_000_000, product.getStock());
    }

    @Test
    void updateStock_ShouldThrowException_WhenNegative() {
        Product product = Product.builder()
                .id("product-1")
                .name("Test Product")
                .stock(100)
                .build();

        InvalidDomainException exception = assertThrows(
                InvalidDomainException.class,
                () -> product.updateStock(-1)
        );

        assertTrue(exception.getMessage().contains("must be between"));
    }

    @Test
    void updateStock_ShouldThrowException_WhenExceedsMaximum() {
        Product product = Product.builder()
                .id("product-1")
                .name("Test Product")
                .stock(100)
                .build();

        InvalidDomainException exception = assertThrows(
                InvalidDomainException.class,
                () -> product.updateStock(1_000_001)
        );

        assertTrue(exception.getMessage().contains("must be between"));
    }

    @Test
    void incrementStock_ShouldThrowException_WhenExceedsMaximum() {
        Product product = Product.builder()
                .id("product-1")
                .name("Test Product")
                .stock(999_999)
                .build();

        InvalidDomainException exception = assertThrows(
                InvalidDomainException.class,
                () -> product.incrementStock(10)
        );

        assertTrue(exception.getMessage().contains("cannot exceed maximum"));
    }

    @Test
    void decrementStock_ShouldThrowException_WhenResultsInNegative() {
        Product product = Product.builder()
                .id("product-1")
                .name("Test Product")
                .stock(50)
                .build();

        InvalidDomainException exception = assertThrows(
                InvalidDomainException.class,
                () -> product.decrementStock(100)
        );

        assertTrue(exception.getMessage().contains("Insufficient stock"));
    }

    @Test
    void hasStock_ShouldReturnTrue_WhenStockIsPositive() {
        Product product = Product.builder()
                .id("product-1")
                .name("Test Product")
                .stock(1)
                .build();

        assertTrue(product.hasStock());
    }

    @Test
    void hasStock_ShouldReturnFalse_WhenStockIsZero() {
        Product product = Product.builder()
                .id("product-1")
                .name("Test Product")
                .stock(0)
                .build();

        assertFalse(product.hasStock());
    }
}
