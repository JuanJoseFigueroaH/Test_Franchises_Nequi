package com.nequi.franchise.infrastructure.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class ApplicationMetrics {

    private final Counter franchiseCreated;
    private final Counter branchAdded;
    private final Counter productAdded;
    private final Counter productDeleted;
    private final Counter stockUpdated;
    private final Counter nameUpdated;
    private final Counter businessRuleViolations;
    private final Counter optimisticLockFailures;
    
    private final Timer franchiseOperationTimer;
    private final Timer branchOperationTimer;
    private final Timer productOperationTimer;

    public ApplicationMetrics(MeterRegistry meterRegistry) {
        this.franchiseCreated = Counter.builder("franchise.created")
                .description("Number of franchises created")
                .tag("operation", "create")
                .register(meterRegistry);

        this.branchAdded = Counter.builder("branch.added")
                .description("Number of branches added")
                .tag("operation", "add")
                .register(meterRegistry);

        this.productAdded = Counter.builder("product.added")
                .description("Number of products added")
                .tag("operation", "add")
                .register(meterRegistry);

        this.productDeleted = Counter.builder("product.deleted")
                .description("Number of products deleted")
                .tag("operation", "delete")
                .register(meterRegistry);

        this.stockUpdated = Counter.builder("product.stock.updated")
                .description("Number of stock updates")
                .tag("operation", "update")
                .register(meterRegistry);

        this.nameUpdated = Counter.builder("entity.name.updated")
                .description("Number of name updates")
                .tag("operation", "update")
                .register(meterRegistry);

        this.businessRuleViolations = Counter.builder("business.rule.violations")
                .description("Number of business rule violations")
                .tag("type", "validation")
                .register(meterRegistry);

        this.optimisticLockFailures = Counter.builder("optimistic.lock.failures")
                .description("Number of optimistic lock failures")
                .tag("type", "concurrency")
                .register(meterRegistry);

        this.franchiseOperationTimer = Timer.builder("franchise.operation.duration")
                .description("Time taken for franchise operations")
                .register(meterRegistry);

        this.branchOperationTimer = Timer.builder("branch.operation.duration")
                .description("Time taken for branch operations")
                .register(meterRegistry);

        this.productOperationTimer = Timer.builder("product.operation.duration")
                .description("Time taken for product operations")
                .register(meterRegistry);
    }

    public void recordFranchiseCreated() {
        franchiseCreated.increment();
    }

    public void recordBranchAdded() {
        branchAdded.increment();
    }

    public void recordProductAdded() {
        productAdded.increment();
    }

    public void recordProductDeleted() {
        productDeleted.increment();
    }

    public void recordStockUpdated() {
        stockUpdated.increment();
    }

    public void recordNameUpdated() {
        nameUpdated.increment();
    }

    public void recordBusinessRuleViolation() {
        businessRuleViolations.increment();
    }

    public void recordOptimisticLockFailure() {
        optimisticLockFailures.increment();
    }

    public Timer.Sample startFranchiseTimer() {
        return Timer.start();
    }

    public void recordFranchiseOperation(Timer.Sample sample) {
        sample.stop(franchiseOperationTimer);
    }

    public Timer.Sample startBranchTimer() {
        return Timer.start();
    }

    public void recordBranchOperation(Timer.Sample sample) {
        sample.stop(branchOperationTimer);
    }

    public Timer.Sample startProductTimer() {
        return Timer.start();
    }

    public void recordProductOperation(Timer.Sample sample) {
        sample.stop(productOperationTimer);
    }
}
