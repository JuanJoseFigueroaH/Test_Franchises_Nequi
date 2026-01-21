package com.nequi.franchise.infrastructure.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class CacheMetrics {

    private final Counter cacheHits;
    private final Counter cacheMisses;
    private final Counter cacheErrors;
    private final Counter cacheSetSuccess;
    private final Counter cacheSetFailure;
    private final Counter cacheDeleteSuccess;
    private final Counter cacheDeleteFailure;
    private final Timer cacheGetTimer;
    private final Timer cacheSetTimer;
    private final Timer cacheDeleteTimer;

    public CacheMetrics(MeterRegistry meterRegistry) {
        this.cacheHits = Counter.builder("cache.hits")
                .description("Number of cache hits")
                .tag("cache", "redis")
                .register(meterRegistry);

        this.cacheMisses = Counter.builder("cache.misses")
                .description("Number of cache misses")
                .tag("cache", "redis")
                .register(meterRegistry);

        this.cacheErrors = Counter.builder("cache.errors")
                .description("Number of cache errors")
                .tag("cache", "redis")
                .register(meterRegistry);

        this.cacheSetSuccess = Counter.builder("cache.set.success")
                .description("Number of successful cache set operations")
                .tag("cache", "redis")
                .register(meterRegistry);

        this.cacheSetFailure = Counter.builder("cache.set.failure")
                .description("Number of failed cache set operations")
                .tag("cache", "redis")
                .register(meterRegistry);

        this.cacheDeleteSuccess = Counter.builder("cache.delete.success")
                .description("Number of successful cache delete operations")
                .tag("cache", "redis")
                .register(meterRegistry);

        this.cacheDeleteFailure = Counter.builder("cache.delete.failure")
                .description("Number of failed cache delete operations")
                .tag("cache", "redis")
                .register(meterRegistry);

        this.cacheGetTimer = Timer.builder("cache.get.duration")
                .description("Time taken to get from cache")
                .tag("cache", "redis")
                .register(meterRegistry);

        this.cacheSetTimer = Timer.builder("cache.set.duration")
                .description("Time taken to set in cache")
                .tag("cache", "redis")
                .register(meterRegistry);

        this.cacheDeleteTimer = Timer.builder("cache.delete.duration")
                .description("Time taken to delete from cache")
                .tag("cache", "redis")
                .register(meterRegistry);
    }

    public void recordCacheHit() {
        cacheHits.increment();
    }

    public void recordCacheMiss() {
        cacheMisses.increment();
    }

    public void recordCacheError() {
        cacheErrors.increment();
    }

    public void recordCacheSetSuccess() {
        cacheSetSuccess.increment();
    }

    public void recordCacheSetFailure() {
        cacheSetFailure.increment();
    }

    public void recordCacheDeleteSuccess() {
        cacheDeleteSuccess.increment();
    }

    public void recordCacheDeleteFailure() {
        cacheDeleteFailure.increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start();
    }

    public void recordGetDuration(Timer.Sample sample) {
        sample.stop(cacheGetTimer);
    }

    public void recordSetDuration(Timer.Sample sample) {
        sample.stop(cacheSetTimer);
    }

    public void recordDeleteDuration(Timer.Sample sample) {
        sample.stop(cacheDeleteTimer);
    }
}
