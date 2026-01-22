package com.nequi.franchise.infrastructure.adapter.output.cache;

import com.nequi.franchise.infrastructure.config.CacheMetrics;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisCacheAdapterTest {

    @Mock
    private ReactiveRedisTemplate<String, Object> redisTemplate;

    @Mock
    private ReactiveValueOperations<String, Object> valueOperations;

    @Mock
    private CacheMetrics cacheMetrics;

    @Mock
    private Timer.Sample timerSample;

    private CircuitBreaker circuitBreaker;

    private RedisCacheAdapter cacheAdapter;

    @BeforeEach
    void setUp() {
        circuitBreaker = CircuitBreaker.of("testCircuitBreaker", CircuitBreakerConfig.ofDefaults());
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(cacheMetrics.startTimer()).thenReturn(timerSample);
        cacheAdapter = new RedisCacheAdapter(redisTemplate, circuitBreaker, cacheMetrics);
    }

    @Test
    void get_ShouldReturnValueWhenExists() {
        String testValue = "test-value";
        when(valueOperations.get(anyString())).thenReturn(Mono.just(testValue));

        var result = cacheAdapter.get("test-key", String.class);

        StepVerifier.create(result)
                .expectNext(testValue)
                .verifyComplete();

        verify(valueOperations, times(1)).get("test-key");
    }

    @Test
    void get_ShouldReturnEmptyWhenNotExists() {
        when(valueOperations.get(anyString())).thenReturn(Mono.empty());

        var result = cacheAdapter.get("test-key", String.class);

        StepVerifier.create(result)
                .verifyComplete();

        verify(valueOperations, times(1)).get("test-key");
    }

    @Test
    void get_ShouldReturnEmptyOnError() {
        when(valueOperations.get(anyString())).thenReturn(Mono.error(new RuntimeException("Redis error")));

        var result = cacheAdapter.get("test-key", String.class);

        StepVerifier.create(result)
                .verifyComplete();

        verify(valueOperations, times(1)).get("test-key");
    }

    @Test
    void set_ShouldReturnTrueWhenSuccessful() {
        when(valueOperations.set(anyString(), any(), any(Duration.class))).thenReturn(Mono.just(true));

        var result = cacheAdapter.set("test-key", "test-value", Duration.ofMinutes(30));

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        verify(valueOperations, times(1)).set(eq("test-key"), eq("test-value"), any(Duration.class));
    }

    @Test
    void set_ShouldReturnFalseOnError() {
        when(valueOperations.set(anyString(), any(), any(Duration.class)))
                .thenReturn(Mono.error(new RuntimeException("Redis error")));

        var result = cacheAdapter.set("test-key", "test-value", Duration.ofMinutes(30));

        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();

        verify(valueOperations, times(1)).set(eq("test-key"), eq("test-value"), any(Duration.class));
    }

    @Test
    void delete_ShouldReturnTrueWhenDeleted() {
        when(redisTemplate.delete(anyString())).thenReturn(Mono.just(1L));

        var result = cacheAdapter.delete("test-key");

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        verify(redisTemplate, times(1)).delete("test-key");
    }

    @Test
    void delete_ShouldReturnFalseWhenNotFound() {
        when(redisTemplate.delete(anyString())).thenReturn(Mono.just(0L));

        var result = cacheAdapter.delete("test-key");

        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();

        verify(redisTemplate, times(1)).delete("test-key");
    }

    @Test
    void deleteByPattern_ShouldReturnCountOfDeletedKeys() {
        when(redisTemplate.keys(anyString())).thenReturn(reactor.core.publisher.Flux.just("key1", "key2", "key3"));
        when(redisTemplate.delete(anyString())).thenReturn(Mono.just(1L));

        var result = cacheAdapter.deleteByPattern("franchise:*");

        StepVerifier.create(result)
                .expectNext(3L)
                .verifyComplete();

        verify(redisTemplate, times(1)).keys("franchise:*");
        verify(redisTemplate, times(3)).delete(anyString());
    }

    @Test
    void deleteByPattern_ShouldReturnZeroWhenNoKeysFound() {
        when(redisTemplate.keys(anyString())).thenReturn(reactor.core.publisher.Flux.empty());

        var result = cacheAdapter.deleteByPattern("franchise:*");

        StepVerifier.create(result)
                .expectNext(0L)
                .verifyComplete();

        verify(redisTemplate, times(1)).keys("franchise:*");
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void deleteByPattern_ShouldReturnZeroOnError() {
        when(redisTemplate.keys(anyString())).thenReturn(reactor.core.publisher.Flux.error(new RuntimeException("Redis error")));

        var result = cacheAdapter.deleteByPattern("franchise:*");

        StepVerifier.create(result)
                .expectNext(0L)
                .verifyComplete();

        verify(redisTemplate, times(1)).keys("franchise:*");
    }
}
