package com.nequi.franchise.domain.model;

import com.nequi.franchise.domain.exception.DuplicateEntityException;
import com.nequi.franchise.domain.exception.InvalidDomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FranchiseBusinessRulesTest {

    @Test
    void addBranch_ShouldThrowException_WhenDuplicateName() {
        Franchise franchise = Franchise.builder()
                .id("franchise-1")
                .name("Test Franchise")
                .build();

        Branch branch1 = Branch.builder()
                .id("branch-1")
                .name("Main Branch")
                .build();

        Branch branch2 = Branch.builder()
                .id("branch-2")
                .name("Main Branch")
                .build();

        franchise.addBranch(branch1);

        DuplicateEntityException exception = assertThrows(
                DuplicateEntityException.class,
                () -> franchise.addBranch(branch2)
        );

        assertTrue(exception.getMessage().contains("already exists"));
        assertTrue(exception.getMessage().contains("Main Branch"));
    }

    @Test
    void addBranch_ShouldThrowException_WhenDuplicateNameCaseInsensitive() {
        Franchise franchise = Franchise.builder()
                .id("franchise-1")
                .name("Test Franchise")
                .build();

        Branch branch1 = Branch.builder()
                .id("branch-1")
                .name("Main Branch")
                .build();

        Branch branch2 = Branch.builder()
                .id("branch-2")
                .name("MAIN BRANCH")
                .build();

        franchise.addBranch(branch1);

        DuplicateEntityException exception = assertThrows(
                DuplicateEntityException.class,
                () -> franchise.addBranch(branch2)
        );

        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    void addBranch_ShouldSucceed_WhenDifferentNames() {
        Franchise franchise = Franchise.builder()
                .id("franchise-1")
                .name("Test Franchise")
                .build();

        Branch branch1 = Branch.builder()
                .id("branch-1")
                .name("Branch One")
                .build();

        Branch branch2 = Branch.builder()
                .id("branch-2")
                .name("Branch Two")
                .build();

        assertDoesNotThrow(() -> {
            franchise.addBranch(branch1);
            franchise.addBranch(branch2);
        });

        assertEquals(2, franchise.getBranchCount());
    }

    @Test
    void addBranch_ShouldThrowException_WhenExceedsMaxBranches() {
        Franchise franchise = Franchise.builder()
                .id("franchise-1")
                .name("Test Franchise")
                .build();

        for (int i = 0; i < 500; i++) {
            Branch branch = Branch.builder()
                    .id("branch-" + i)
                    .name("Branch " + i)
                    .build();
            franchise.addBranch(branch);
        }

        Branch extraBranch = Branch.builder()
                .id("branch-501")
                .name("Extra Branch")
                .build();

        InvalidDomainException exception = assertThrows(
                InvalidDomainException.class,
                () -> franchise.addBranch(extraBranch)
        );

        assertTrue(exception.getMessage().contains("cannot have more than"));
        assertTrue(exception.getMessage().contains("500"));
    }

    @Test
    void hasBranchWithName_ShouldReturnTrue_WhenNameExists() {
        Franchise franchise = Franchise.builder()
                .id("franchise-1")
                .name("Test Franchise")
                .build();

        Branch branch = Branch.builder()
                .id("branch-1")
                .name("Test Branch")
                .build();

        franchise.addBranch(branch);

        assertTrue(franchise.hasBranchWithName("Test Branch"));
        assertTrue(franchise.hasBranchWithName("test branch"));
        assertTrue(franchise.hasBranchWithName("TEST BRANCH"));
    }

    @Test
    void hasBranchWithName_ShouldReturnFalse_WhenNameDoesNotExist() {
        Franchise franchise = Franchise.builder()
                .id("franchise-1")
                .name("Test Franchise")
                .build();

        assertFalse(franchise.hasBranchWithName("Non-existent Branch"));
    }
}
