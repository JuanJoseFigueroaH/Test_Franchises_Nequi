package com.nequi.franchise.infrastructure.observability;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component("redis")
public class CustomHealthIndicator implements ReactiveHealthIndicator {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    public CustomHealthIndicator(ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Health> health() {
        return checkRedisHealth()
                .timeout(Duration.ofSeconds(2))
                .onErrorResume(ex -> Mono.just(
                    Health.down()
                        .withDetail("error", ex.getMessage())
                        .withDetail("errorType", ex.getClass().getSimpleName())
                        .build()
                ));
    }

    private Mono<Health> checkRedisHealth() {
        return redisTemplate.execute(connection -> connection.ping())
                .next()
                .map(response -> Health.up()
                    .withDetail("redis", "available")
                    .withDetail("response", response)
                    .build())
                .defaultIfEmpty(Health.down()
                    .withDetail("redis", "no response")
                    .build());
    }
}
