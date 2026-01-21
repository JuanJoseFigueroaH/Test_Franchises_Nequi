package com.nequi.franchise.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CacheCircuitBreakerConfig {

    @Bean
    public CircuitBreaker cacheCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slowCallRateThreshold(50)
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(5)
                .minimumNumberOfCalls(10)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(20)
                .recordExceptions(
                        io.lettuce.core.RedisConnectionException.class,
                        io.lettuce.core.RedisCommandTimeoutException.class,
                        java.util.concurrent.TimeoutException.class
                )
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        CircuitBreaker circuitBreaker = registry.circuitBreaker("redisCache");

        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> 
                    org.slf4j.LoggerFactory.getLogger(CacheCircuitBreakerConfig.class)
                        .warn("Redis Cache Circuit Breaker state transition: {} -> {}", 
                            event.getStateTransition().getFromState(),
                            event.getStateTransition().getToState())
                )
                .onError(event ->
                    org.slf4j.LoggerFactory.getLogger(CacheCircuitBreakerConfig.class)
                        .error("Redis Cache Circuit Breaker error: {}", event.getThrowable().getMessage())
                )
                .onSuccess(event ->
                    org.slf4j.LoggerFactory.getLogger(CacheCircuitBreakerConfig.class)
                        .debug("Redis Cache Circuit Breaker success")
                );

        return circuitBreaker;
    }
}
