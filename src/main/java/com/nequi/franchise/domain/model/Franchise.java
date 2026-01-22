package com.nequi.franchise.domain.model;

import com.nequi.franchise.domain.exception.BranchNotFoundException;
import com.nequi.franchise.domain.exception.DuplicateEntityException;
import com.nequi.franchise.domain.exception.InvalidDomainException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Franchise {
    private static final int MIN_NAME_LENGTH = 1;
    private static final int MAX_NAME_LENGTH = 200;
    private static final int MAX_BRANCHES = 500;

    private String id;
    
    @Setter(AccessLevel.PACKAGE)
    private String name;
    
    @Builder.Default
    private List<Branch> branches = new ArrayList<>();
    
    private Long version;

    public void updateName(String newName) {
        validateName(newName);
        this.name = newName;
    }

    public void addBranch(Branch branch) {
        if (branch == null) {
            throw new InvalidDomainException("Branch cannot be null");
        }
        if (branches.size() >= MAX_BRANCHES) {
            throw new InvalidDomainException("Franchise cannot have more than " + MAX_BRANCHES + " branches");
        }
        if (hasBranch(branch.getId())) {
            throw new DuplicateEntityException("Branch with id " + branch.getId() + " already exists in this franchise");
        }
        if (hasBranchWithName(branch.getName())) {
            throw new DuplicateEntityException("Branch with name '" + branch.getName() + "' already exists in this franchise");
        }
        this.branches.add(branch);
    }

    public void removeBranch(String branchId) {
        if (branchId == null || branchId.trim().isEmpty()) {
            throw new InvalidDomainException("Branch ID cannot be null or empty");
        }
        boolean removed = this.branches.removeIf(b -> b.getId().equals(branchId));
        if (!removed) {
            throw new BranchNotFoundException("Branch not found with id: " + branchId);
        }
    }

    public Branch findBranch(String branchId) {
        if (branchId == null || branchId.trim().isEmpty()) {
            throw new InvalidDomainException("Branch ID cannot be null or empty");
        }
        return this.branches.stream()
                .filter(b -> b.getId().equals(branchId))
                .findFirst()
                .orElseThrow(() -> new BranchNotFoundException("Branch not found with id: " + branchId));
    }

    public List<Branch> getBranches() {
        return Collections.unmodifiableList(branches);
    }

    public int getBranchCount() {
        return this.branches.size();
    }

    public boolean hasBranches() {
        return !this.branches.isEmpty();
    }

    public boolean hasBranch(String branchId) {
        if (branchId == null) {
            return false;
        }
        return this.branches.stream()
                .anyMatch(b -> b.getId().equals(branchId));
    }

    public boolean hasBranchWithName(String branchName) {
        if (branchName == null || branchName.trim().isEmpty()) {
            return false;
        }
        return this.branches.stream()
                .anyMatch(b -> b.getName().equalsIgnoreCase(branchName.trim()));
    }

    public int getTotalProductCount() {
        return this.branches.stream()
                .mapToInt(Branch::getProductCount)
                .sum();
    }

    public Franchise getMaxStockProductsPerBranch() {
        List<Branch> filteredBranches = this.branches.stream()
                .filter(Branch::hasProducts)
                .map(branch -> {
                    Optional<Product> maxProduct = branch.getProductWithMaxStock();
                    if (maxProduct.isPresent()) {
                        Branch newBranch = Branch.builder()
                                .id(branch.getId())
                                .name(branch.getName())
                                .build();
                        newBranch.addProduct(maxProduct.get());
                        return newBranch;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();

        return Franchise.builder()
                .id(this.id)
                .name(this.name)
                .branches(new ArrayList<>(filteredBranches))
                .build();
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidDomainException("Franchise name cannot be null or empty");
        }
        if (name.length() < MIN_NAME_LENGTH || name.length() > MAX_NAME_LENGTH) {
            throw new InvalidDomainException(
                String.format("Franchise name must be between %d and %d characters", MIN_NAME_LENGTH, MAX_NAME_LENGTH)
            );
        }
    }

    public void incrementVersion() {
        this.version = (this.version == null) ? 1L : this.version + 1;
    }

    public static class FranchiseBuilder {
        private List<Branch> branches$value;
        private boolean branches$set;
        private Long version$value;
        private boolean version$set;

        public FranchiseBuilder branches(List<Branch> branches) {
            this.branches$value = branches;
            this.branches$set = true;
            return this;
        }

        public FranchiseBuilder version(Long version) {
            this.version$value = version;
            this.version$set = true;
            return this;
        }

        public Franchise build() {
            List<Branch> branchesValue = this.branches$set ? this.branches$value : new ArrayList<>();
            Long versionValue = this.version$set ? this.version$value : 0L;
            if (branchesValue == null) {
                branchesValue = new ArrayList<>();
            }
            Franchise franchise = new Franchise(id, name, branchesValue, versionValue);
            franchise.validateName(name);
            return franchise;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Franchise franchise = (Franchise) o;
        return Objects.equals(id, franchise.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
