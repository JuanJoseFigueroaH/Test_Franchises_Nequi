package com.nequi.franchise.infrastructure.observability;

import com.nequi.franchise.domain.exception.InvalidDomainException;
import com.nequi.franchise.domain.exception.OptimisticLockException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Aspect
@Component
public class MetricsInterceptor {

    private final ApplicationMetrics applicationMetrics;

    public MetricsInterceptor(ApplicationMetrics applicationMetrics) {
        this.applicationMetrics = applicationMetrics;
    }

    @Around("execution(* com.nequi.franchise.application.service.CreateFranchiseService.execute(..))")
    public Object trackFranchiseCreation(ProceedingJoinPoint joinPoint) throws Throwable {
        io.micrometer.core.instrument.Timer.Sample sample = applicationMetrics.startFranchiseTimer();
        
        Object result = joinPoint.proceed();
        
        if (result instanceof Mono) {
            return ((Mono<?>) result)
                .doOnSuccess(value -> {
                    applicationMetrics.recordFranchiseCreated();
                    applicationMetrics.recordFranchiseOperation(sample);
                })
                .doOnError(this::handleError);
        }
        
        return result;
    }

    @Around("execution(* com.nequi.franchise.application.service.AddBranchToFranchiseService.execute(..))")
    public Object trackBranchAddition(ProceedingJoinPoint joinPoint) throws Throwable {
        io.micrometer.core.instrument.Timer.Sample sample = applicationMetrics.startBranchTimer();
        
        Object result = joinPoint.proceed();
        
        if (result instanceof Mono) {
            return ((Mono<?>) result)
                .doOnSuccess(value -> {
                    applicationMetrics.recordBranchAdded();
                    applicationMetrics.recordBranchOperation(sample);
                })
                .doOnError(this::handleError);
        }
        
        return result;
    }

    @Around("execution(* com.nequi.franchise.application.service.AddProductToBranchService.execute(..))")
    public Object trackProductAddition(ProceedingJoinPoint joinPoint) throws Throwable {
        io.micrometer.core.instrument.Timer.Sample sample = applicationMetrics.startProductTimer();
        
        Object result = joinPoint.proceed();
        
        if (result instanceof Mono) {
            return ((Mono<?>) result)
                .doOnSuccess(value -> {
                    applicationMetrics.recordProductAdded();
                    applicationMetrics.recordProductOperation(sample);
                })
                .doOnError(this::handleError);
        }
        
        return result;
    }

    @Around("execution(* com.nequi.franchise.application.service.DeleteProductFromBranchService.execute(..))")
    public Object trackProductDeletion(ProceedingJoinPoint joinPoint) throws Throwable {
        io.micrometer.core.instrument.Timer.Sample sample = applicationMetrics.startProductTimer();
        
        Object result = joinPoint.proceed();
        
        if (result instanceof Mono) {
            return ((Mono<?>) result)
                .doOnSuccess(value -> {
                    applicationMetrics.recordProductDeleted();
                    applicationMetrics.recordProductOperation(sample);
                })
                .doOnError(this::handleError);
        }
        
        return result;
    }

    @Around("execution(* com.nequi.franchise.application.service.UpdateProductStockService.execute(..))")
    public Object trackStockUpdate(ProceedingJoinPoint joinPoint) throws Throwable {
        io.micrometer.core.instrument.Timer.Sample sample = applicationMetrics.startProductTimer();
        
        Object result = joinPoint.proceed();
        
        if (result instanceof Mono) {
            return ((Mono<?>) result)
                .doOnSuccess(value -> {
                    applicationMetrics.recordStockUpdated();
                    applicationMetrics.recordProductOperation(sample);
                })
                .doOnError(this::handleError);
        }
        
        return result;
    }

    @Around("execution(* com.nequi.franchise.application.service.Update*NameService.execute(..))")
    public Object trackNameUpdate(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        
        if (result instanceof Mono) {
            return ((Mono<?>) result)
                .doOnSuccess(value -> applicationMetrics.recordNameUpdated())
                .doOnError(this::handleError);
        }
        
        return result;
    }

    private void handleError(Throwable error) {
        if (error instanceof InvalidDomainException) {
            applicationMetrics.recordBusinessRuleViolation();
        } else if (error instanceof OptimisticLockException) {
            applicationMetrics.recordOptimisticLockFailure();
        }
    }
}
