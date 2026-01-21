package com.nequi.franchise.infrastructure.adapter.output.cache;

import com.nequi.franchise.infrastructure.config.CacheMetrics;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisCacheAdapterEnhancedTest {

    @Mock
    private ReactiveRedisTemplate<String, Object> redisTemplate;

    @Mock
    private ReactiveValueOperations<String, Object> valueOperations;

    @Mock
    private CircuitBreaker circuitBreaker;

    @Mock
    private CacheMetrics cacheMetrics;

    private RedisCacheAdapter cacheAdapter;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(circuitBreaker.decorateSupplier(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(cacheMetrics.startTimer()).thenReturn(mock(io.micrometer.core.instrument.Timer.Sample.class));
        
        cacheAdapter = new RedisCacheAdapter(redisTemplate, circuitBreaker, cacheMetrics);
    }

    @Test
    void get_ShouldRecordCacheHit_WhenValueExists() {
        String testValue = "test-value";
        when(valueOperations.get(anyString())).thenReturn(Mono.just(testValue));

        var result = cacheAdapter.get("test-key", String.class);

        StepVerifier.create(result)
                .expectNext(testValue)
                .verifyComplete();

        verify(cacheMetrics, times(1)).recordCacheHit();
        verify(cacheMetrics, times(1)).recordGetDuration(any());
    }

    @Test
    void get_ShouldRecordCacheMiss_WhenValueDoesNotExist() {
        when(valueOperations.get(anyString())).thenReturn(Mono.empty());

        var result = cacheAdapter.get("test-key", String.class);

        StepVerifier.create(result)
                .verifyComplete();

        verify(cacheMetrics, times(1)).recordCacheMiss();
        verify(cacheMetrics, times(1)).recordGetDuration(any());
    }

    @Test
    void get_ShouldRecordCacheError_WhenExceptionOccurs() {
        when(valueOperations.get(anyString())).thenReturn(Mono.error(new RuntimeException("Redis error")));

        var result = cacheAdapter.get("test-key", String.class);

        StepVerifier.create(result)
                .verifyComplete();

        verify(cacheMetrics, times(1)).recordCacheError();
        verify(cacheMetrics, times(1)).recordGetDuration(any());
    }

    @Test
    void set_ShouldRecordSuccess_WhenOperationSucceeds() {
        when(valueOperations.set(anyString(), any(), any(Duration.class))).thenReturn(Mono.just(true));

        var result = cacheAdapter.set("test-key", "test-value", Duration.ofMinutes(30));

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        verify(cacheMetrics, times(1)).recordCacheSetSuccess();
        verify(cacheMetrics, times(1)).recordSetDuration(any());
    }

    @Test
    void set_ShouldRecordFailure_WhenOperationFails() {
        when(valueOperations.set(anyString(), any(), any(Duration.class)))
                .thenReturn(Mono.error(new RuntimeException("Redis error")));

        var result = cacheAdapter.set("test-key", "test-value", Duration.ofMinutes(30));

        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();

        verify(cacheMetrics, times(1)).recordCacheSetFailure();
        verify(cacheMetrics, times(1)).recordCacheError();
        verify(cacheMetrics, times(1)).recordSetDuration(any());
    }

    @Test
    void delete_ShouldRecordSuccess_WhenKeyIsDeleted() {
        when(redisTemplate.delete(anyString())).thenReturn(Mono.just(1L));

        var result = cacheAdapter.delete("test-key");

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        verify(cacheMetrics, times(1)).recordCacheDeleteSuccess();
        verify(cacheMetrics, times(1)).recordDeleteDuration(any());
    }

    @Test
    void delete_ShouldRecordFailure_WhenExceptionOccurs() {
        when(redisTemplate.delete(anyString())).thenReturn(Mono.error(new RuntimeException("Redis error")));

        var result = cacheAdapter.delete("test-key");

        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();

        verify(cacheMetrics, times(1)).recordCacheDeleteFailure();
        verify(cacheMetrics, times(1)).recordCacheError();
        verify(cacheMetrics, times(1)).recordDeleteDuration(any());
    }

    @Test
    void deleteByPattern_ShouldRecordSuccess_WhenKeysAreDeleted() {
        when(redisTemplate.keys(anyString())).thenReturn(Flux.just("key1", "key2"));
        when(redisTemplate.delete(anyString())).thenReturn(Mono.just(1L));

        var result = cacheAdapter.deleteByPattern("franchise:*");

        StepVerifier.create(result)
                .expectNext(2L)
                .verifyComplete();

        verify(cacheMetrics, times(1)).recordCacheDeleteSuccess();
        verify(cacheMetrics, times(1)).recordDeleteDuration(any());
    }

    @Test
    void deleteByPattern_ShouldHandlePartialFailures() {
        when(redisTemplate.keys(anyString())).thenReturn(Flux.just("key1", "key2", "key3"));
        when(redisTemplate.delete("key1")).thenReturn(Mono.just(1L));
        when(redisTemplate.delete("key2")).thenReturn(Mono.error(new RuntimeException("Error")));
        when(redisTemplate.delete("key3")).thenReturn(Mono.just(1L));

        var result = cacheAdapter.deleteByPattern("franchise:*");

        StepVerifier.create(result)
                .expectNext(2L)
                .verifyComplete();

        verify(cacheMetrics, times(1)).recordDeleteDuration(any());
    }
}
