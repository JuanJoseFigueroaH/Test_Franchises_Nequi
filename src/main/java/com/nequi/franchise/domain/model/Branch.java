package com.nequi.franchise.domain.model;

import com.nequi.franchise.domain.exception.DuplicateEntityException;
import com.nequi.franchise.domain.exception.InvalidDomainException;
import com.nequi.franchise.domain.exception.ProductNotFoundException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Branch {
    private static final int MIN_NAME_LENGTH = 1;
    private static final int MAX_NAME_LENGTH = 200;
    private static final int MAX_PRODUCTS = 1000;

    private String id;
    
    @Setter(AccessLevel.PACKAGE)
    private String name;
    
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    public void updateName(String newName) {
        validateName(newName);
        this.name = newName;
    }

    public void addProduct(Product product) {
        if (product == null) {
            throw new InvalidDomainException("Product cannot be null");
        }
        if (products.size() >= MAX_PRODUCTS) {
            throw new InvalidDomainException("Branch cannot have more than " + MAX_PRODUCTS + " products");
        }
        if (hasProduct(product.getId())) {
            throw new DuplicateEntityException("Product with id " + product.getId() + " already exists in this branch");
        }
        this.products.add(product);
    }

    public void removeProduct(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new InvalidDomainException("Product ID cannot be null or empty");
        }
        boolean removed = this.products.removeIf(p -> p.getId().equals(productId));
        if (!removed) {
            throw new ProductNotFoundException("Product not found with id: " + productId);
        }
    }

    public Product findProduct(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new InvalidDomainException("Product ID cannot be null or empty");
        }
        return this.products.stream()
                .filter(p -> p.getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));
    }

    public Optional<Product> getProductWithMaxStock() {
        return this.products.stream()
                .max(Comparator.comparing(Product::getStock));
    }

    public List<Product> getProducts() {
        return Collections.unmodifiableList(products);
    }

    public int getProductCount() {
        return this.products.size();
    }

    public boolean hasProducts() {
        return !this.products.isEmpty();
    }

    public boolean hasProduct(String productId) {
        if (productId == null) {
            return false;
        }
        return this.products.stream()
                .anyMatch(p -> p.getId().equals(productId));
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidDomainException("Branch name cannot be null or empty");
        }
        if (name.length() < MIN_NAME_LENGTH || name.length() > MAX_NAME_LENGTH) {
            throw new InvalidDomainException(
                String.format("Branch name must be between %d and %d characters", MIN_NAME_LENGTH, MAX_NAME_LENGTH)
            );
        }
    }

    public static class BranchBuilder {
        public Branch build() {
            if (products == null) {
                products = new ArrayList<>();
            }
            Branch branch = new Branch(id, name, products);
            branch.validateName(name);
            return branch;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Branch branch = (Branch) o;
        return Objects.equals(id, branch.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
