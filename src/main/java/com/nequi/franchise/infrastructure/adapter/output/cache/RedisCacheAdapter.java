package com.nequi.franchise.infrastructure.adapter.output.cache;

import com.nequi.franchise.domain.port.output.CachePort;
import com.nequi.franchise.infrastructure.config.CacheMetrics;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class RedisCacheAdapter implements CachePort {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheAdapter.class);

    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final CircuitBreaker circuitBreaker;
    private final CacheMetrics cacheMetrics;

    public RedisCacheAdapter(
            ReactiveRedisTemplate<String, Object> redisTemplate,
            CircuitBreaker cacheCircuitBreaker,
            CacheMetrics cacheMetrics) {
        this.redisTemplate = redisTemplate;
        this.circuitBreaker = cacheCircuitBreaker;
        this.cacheMetrics = cacheMetrics;
    }

    @Override
    public <T> Mono<T> get(String key, Class<T> type) {
        Timer.Sample sample = cacheMetrics.startTimer();
        
        return redisTemplate.opsForValue()
                .get(key)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnNext(value -> {
                    logger.debug("Cache HIT for key: {}", key);
                    cacheMetrics.recordCacheHit();
                })
                .map(type::cast)
                .doOnError(error -> {
                    logger.error("Cache GET error for key: {}. Error: {} - {}", 
                        key, error.getClass().getSimpleName(), error.getMessage());
                    cacheMetrics.recordCacheError();
                })
                .onErrorResume(error -> {
                    logger.warn("Falling back to empty result for key: {} due to: {}", 
                        key, error.getMessage());
                    return Mono.empty();
                })
                .switchIfEmpty(Mono.defer(() -> {
                    logger.debug("Cache MISS for key: {}", key);
                    cacheMetrics.recordCacheMiss();
                    return Mono.empty();
                }))
                .doFinally(signalType -> cacheMetrics.recordGetDuration(sample));
    }

    @Override
    public <T> Mono<Boolean> set(String key, T value, Duration ttl) {
        Timer.Sample sample = cacheMetrics.startTimer();
        
        return redisTemplate.opsForValue()
                .set(key, value, ttl)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnSuccess(success -> {
                    if (success) {
                        logger.debug("Cache SET success for key: {} with TTL: {}", key, ttl);
                        cacheMetrics.recordCacheSetSuccess();
                    } else {
                        logger.warn("Cache SET returned false for key: {}", key);
                        cacheMetrics.recordCacheSetFailure();
                    }
                })
                .doOnError(error -> {
                    logger.error("Cache SET error for key: {}. Error: {} - {}", 
                        key, error.getClass().getSimpleName(), error.getMessage());
                    cacheMetrics.recordCacheSetFailure();
                    cacheMetrics.recordCacheError();
                })
                .onErrorResume(error -> {
                    logger.warn("Falling back to false for SET operation on key: {} due to: {}", 
                        key, error.getMessage());
                    return Mono.just(false);
                })
                .doFinally(signalType -> cacheMetrics.recordSetDuration(sample));
    }

    @Override
    public Mono<Boolean> delete(String key) {
        Timer.Sample sample = cacheMetrics.startTimer();
        
        return redisTemplate.delete(key)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .map(count -> {
                    boolean deleted = count > 0;
                    if (deleted) {
                        logger.debug("Cache DELETE success for key: {}", key);
                        cacheMetrics.recordCacheDeleteSuccess();
                    } else {
                        logger.debug("Cache DELETE: key not found: {}", key);
                    }
                    return deleted;
                })
                .doOnError(error -> {
                    logger.error("Cache DELETE error for key: {}. Error: {} - {}", 
                        key, error.getClass().getSimpleName(), error.getMessage());
                    cacheMetrics.recordCacheDeleteFailure();
                    cacheMetrics.recordCacheError();
                })
                .onErrorResume(error -> {
                    logger.warn("Falling back to false for DELETE operation on key: {} due to: {}", 
                        key, error.getMessage());
                    return Mono.just(false);
                })
                .doFinally(signalType -> cacheMetrics.recordDeleteDuration(sample));
    }

    @Override
    public Mono<Long> deleteByPattern(String pattern) {
        Timer.Sample sample = cacheMetrics.startTimer();
        
        return redisTemplate.keys(pattern)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .flatMap(key -> redisTemplate.delete(key)
                        .doOnNext(count -> {
                            if (count > 0) {
                                logger.debug("Deleted key: {} (pattern: {})", key, pattern);
                            }
                        })
                        .onErrorResume(error -> {
                            logger.error("Error deleting key: {} (pattern: {}). Error: {}", 
                                key, pattern, error.getMessage());
                            return Mono.just(0L);
                        })
                )
                .reduce(0L, Long::sum)
                .doOnSuccess(count -> {
                    logger.info("Cache DELETE by pattern: {} - Deleted {} keys", pattern, count);
                    if (count > 0) {
                        cacheMetrics.recordCacheDeleteSuccess();
                    }
                })
                .doOnError(error -> {
                    logger.error("Cache DELETE by pattern error for pattern: {}. Error: {} - {}", 
                        pattern, error.getClass().getSimpleName(), error.getMessage());
                    cacheMetrics.recordCacheDeleteFailure();
                    cacheMetrics.recordCacheError();
                })
                .onErrorResume(error -> {
                    logger.warn("Falling back to 0 for DELETE by pattern: {} due to: {}", 
                        pattern, error.getMessage());
                    return Mono.just(0L);
                })
                .doFinally(signalType -> cacheMetrics.recordDeleteDuration(sample));
    }
}
