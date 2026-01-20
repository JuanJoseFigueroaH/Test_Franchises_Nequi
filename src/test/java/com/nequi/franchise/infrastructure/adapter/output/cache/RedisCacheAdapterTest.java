package com.nequi.franchise.infrastructure.adapter.output.cache;

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

    private RedisCacheAdapter cacheAdapter;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        cacheAdapter = new RedisCacheAdapter(redisTemplate);
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
}
