package com.nequi.franchise.domain.valueobject;

import com.nequi.franchise.domain.exception.InvalidDomainException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class ProductStock {
    private static final int MIN_STOCK = 0;
    private static final int MAX_STOCK = 1_000_000;
    
    private final Integer value;

    private ProductStock(Integer value) {
        validate(value);
        this.value = value;
    }

    public static ProductStock of(Integer value) {
        return new ProductStock(value);
    }

    public static ProductStock zero() {
        return new ProductStock(0);
    }

    public ProductStock increment(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new InvalidDomainException("Quantity to increment must be positive");
        }
        
        long newStock = (long) this.value + quantity;
        if (newStock > MAX_STOCK) {
            throw new InvalidDomainException(
                String.format("Stock cannot exceed maximum limit of %d. Current: %d, Increment: %d", 
                    MAX_STOCK, this.value, quantity)
            );
        }
        
        return new ProductStock((int) newStock);
    }

    public ProductStock decrement(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new InvalidDomainException("Quantity to decrement must be positive");
        }
        
        int newStock = this.value - quantity;
        if (newStock < MIN_STOCK) {
            throw new InvalidDomainException(
                String.format("Insufficient stock. Available: %d, Requested: %d", this.value, quantity)
            );
        }
        
        return new ProductStock(newStock);
    }

    public boolean hasStock() {
        return this.value > 0;
    }

    public boolean isGreaterThan(ProductStock other) {
        if (other == null) {
            return true;
        }
        return this.value > other.value;
    }

    public boolean canFulfill(Integer requestedQuantity) {
        return requestedQuantity != null && this.value >= requestedQuantity;
    }

    private void validate(Integer value) {
        if (value == null) {
            throw new InvalidDomainException("Stock cannot be null");
        }
        
        if (value < MIN_STOCK) {
            throw new InvalidDomainException(
                String.format("Stock cannot be negative. Provided: %d", value)
            );
        }
        
        if (value > MAX_STOCK) {
            throw new InvalidDomainException(
                String.format("Stock cannot exceed maximum limit of %d. Provided: %d", MAX_STOCK, value)
            );
        }
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
