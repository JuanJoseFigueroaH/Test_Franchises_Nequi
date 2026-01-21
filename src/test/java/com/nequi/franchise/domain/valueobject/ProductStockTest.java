package com.nequi.franchise.domain.valueobject;

import com.nequi.franchise.domain.exception.InvalidDomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductStockTest {

    @Test
    void of_ShouldCreateValidStock() {
        ProductStock stock = ProductStock.of(100);
        
        assertEquals(100, stock.getValue());
    }

    @Test
    void zero_ShouldCreateZeroStock() {
        ProductStock stock = ProductStock.zero();
        
        assertEquals(0, stock.getValue());
    }

    @Test
    void of_ShouldThrowException_WhenNull() {
        InvalidDomainException exception = assertThrows(
            InvalidDomainException.class,
            () -> ProductStock.of(null)
        );
        
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    void of_ShouldThrowException_WhenNegative() {
        InvalidDomainException exception = assertThrows(
            InvalidDomainException.class,
            () -> ProductStock.of(-1)
        );
        
        assertTrue(exception.getMessage().contains("cannot be negative"));
    }

    @Test
    void of_ShouldThrowException_WhenExceedsMaximum() {
        InvalidDomainException exception = assertThrows(
            InvalidDomainException.class,
            () -> ProductStock.of(1_000_001)
        );
        
        assertTrue(exception.getMessage().contains("cannot exceed maximum"));
    }

    @Test
    void increment_ShouldIncreaseStock() {
        ProductStock stock = ProductStock.of(100);
        ProductStock newStock = stock.increment(50);
        
        assertEquals(150, newStock.getValue());
    }

    @Test
    void increment_ShouldThrowException_WhenQuantityIsNull() {
        ProductStock stock = ProductStock.of(100);
        
        InvalidDomainException exception = assertThrows(
            InvalidDomainException.class,
            () -> stock.increment(null)
        );
        
        assertTrue(exception.getMessage().contains("must be positive"));
    }

    @Test
    void increment_ShouldThrowException_WhenQuantityIsZero() {
        ProductStock stock = ProductStock.of(100);
        
        InvalidDomainException exception = assertThrows(
            InvalidDomainException.class,
            () -> stock.increment(0)
        );
        
        assertTrue(exception.getMessage().contains("must be positive"));
    }

    @Test
    void increment_ShouldThrowException_WhenExceedsMaximum() {
        ProductStock stock = ProductStock.of(999_999);
        
        InvalidDomainException exception = assertThrows(
            InvalidDomainException.class,
            () -> stock.increment(10)
        );
        
        assertTrue(exception.getMessage().contains("cannot exceed maximum"));
    }

    @Test
    void decrement_ShouldDecreaseStock() {
        ProductStock stock = ProductStock.of(100);
        ProductStock newStock = stock.decrement(30);
        
        assertEquals(70, newStock.getValue());
    }

    @Test
    void decrement_ShouldThrowException_WhenQuantityIsNull() {
        ProductStock stock = ProductStock.of(100);
        
        InvalidDomainException exception = assertThrows(
            InvalidDomainException.class,
            () -> stock.decrement(null)
        );
        
        assertTrue(exception.getMessage().contains("must be positive"));
    }

    @Test
    void decrement_ShouldThrowException_WhenInsufficientStock() {
        ProductStock stock = ProductStock.of(50);
        
        InvalidDomainException exception = assertThrows(
            InvalidDomainException.class,
            () -> stock.decrement(100)
        );
        
        assertTrue(exception.getMessage().contains("Insufficient stock"));
    }

    @Test
    void hasStock_ShouldReturnTrue_WhenStockIsPositive() {
        ProductStock stock = ProductStock.of(1);
        
        assertTrue(stock.hasStock());
    }

    @Test
    void hasStock_ShouldReturnFalse_WhenStockIsZero() {
        ProductStock stock = ProductStock.zero();
        
        assertFalse(stock.hasStock());
    }

    @Test
    void isGreaterThan_ShouldReturnTrue_WhenGreater() {
        ProductStock stock1 = ProductStock.of(100);
        ProductStock stock2 = ProductStock.of(50);
        
        assertTrue(stock1.isGreaterThan(stock2));
    }

    @Test
    void isGreaterThan_ShouldReturnFalse_WhenLess() {
        ProductStock stock1 = ProductStock.of(50);
        ProductStock stock2 = ProductStock.of(100);
        
        assertFalse(stock1.isGreaterThan(stock2));
    }

    @Test
    void isGreaterThan_ShouldReturnTrue_WhenOtherIsNull() {
        ProductStock stock = ProductStock.of(50);
        
        assertTrue(stock.isGreaterThan(null));
    }

    @Test
    void canFulfill_ShouldReturnTrue_WhenStockIsSufficient() {
        ProductStock stock = ProductStock.of(100);
        
        assertTrue(stock.canFulfill(50));
    }

    @Test
    void canFulfill_ShouldReturnFalse_WhenStockIsInsufficient() {
        ProductStock stock = ProductStock.of(50);
        
        assertFalse(stock.canFulfill(100));
    }

    @Test
    void canFulfill_ShouldReturnFalse_WhenQuantityIsNull() {
        ProductStock stock = ProductStock.of(100);
        
        assertFalse(stock.canFulfill(null));
    }

    @Test
    void equals_ShouldReturnTrue_WhenSameValue() {
        ProductStock stock1 = ProductStock.of(100);
        ProductStock stock2 = ProductStock.of(100);
        
        assertEquals(stock1, stock2);
    }

    @Test
    void equals_ShouldReturnFalse_WhenDifferentValue() {
        ProductStock stock1 = ProductStock.of(100);
        ProductStock stock2 = ProductStock.of(200);
        
        assertNotEquals(stock1, stock2);
    }

    @Test
    void toString_ShouldReturnStringValue() {
        ProductStock stock = ProductStock.of(100);
        
        assertEquals("100", stock.toString());
    }
}
