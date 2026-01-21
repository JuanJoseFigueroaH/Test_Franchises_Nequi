package com.nequi.franchise.domain.model;

import com.nequi.franchise.domain.exception.InvalidDomainException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Product {
    private static final int MIN_NAME_LENGTH = 1;
    private static final int MAX_NAME_LENGTH = 200;
    private static final int MIN_STOCK = 0;
    private static final int MAX_STOCK = 1_000_000;

    private String id;
    
    @Setter(AccessLevel.PACKAGE)
    private String name;
    
    @Setter(AccessLevel.PACKAGE)
    private Integer stock;

    public void updateName(String newName) {
        validateName(newName);
        this.name = newName;
    }

    public void updateStock(Integer newStock) {
        validateStock(newStock);
        this.stock = newStock;
    }

    public void incrementStock(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new InvalidDomainException("Quantity to increment must be positive");
        }
        int newStock = this.stock + quantity;
        if (newStock > MAX_STOCK) {
            throw new InvalidDomainException("Stock cannot exceed maximum limit of " + MAX_STOCK);
        }
        this.stock = newStock;
    }

    public void decrementStock(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new InvalidDomainException("Quantity to decrement must be positive");
        }
        int newStock = this.stock - quantity;
        if (newStock < MIN_STOCK) {
            throw new InvalidDomainException("Insufficient stock. Available: " + this.stock + ", Requested: " + quantity);
        }
        this.stock = newStock;
    }

    public boolean hasStock() {
        return this.stock != null && this.stock > 0;
    }

    public boolean hasMoreStockThan(Product other) {
        if (other == null) {
            return true;
        }
        return this.stock > other.getStock();
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidDomainException("Product name cannot be null or empty");
        }
        if (name.length() < MIN_NAME_LENGTH || name.length() > MAX_NAME_LENGTH) {
            throw new InvalidDomainException(
                String.format("Product name must be between %d and %d characters", MIN_NAME_LENGTH, MAX_NAME_LENGTH)
            );
        }
    }

    private void validateStock(Integer stock) {
        if (stock == null) {
            throw new InvalidDomainException("Product stock cannot be null");
        }
        if (stock < MIN_STOCK || stock > MAX_STOCK) {
            throw new InvalidDomainException(
                String.format("Product stock must be between %d and %d", MIN_STOCK, MAX_STOCK)
            );
        }
    }

    public static class ProductBuilder {
        public Product build() {
            Product product = new Product(id, name, stock);
            product.validateName(name);
            product.validateStock(stock);
            return product;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
