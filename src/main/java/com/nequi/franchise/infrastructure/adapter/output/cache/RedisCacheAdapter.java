package com.nequi.franchise.infrastructure.adapter.output.cache;

import com.nequi.franchise.domain.port.output.CachePort;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class RedisCacheAdapter implements CachePort {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    public RedisCacheAdapter(ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public <T> Mono<T> get(String key, Class<T> type) {
        return redisTemplate.opsForValue()
                .get(key)
                .map(type::cast)
                .onErrorResume(e -> Mono.empty());
    }

    @Override
    public <T> Mono<Boolean> set(String key, T value, Duration ttl) {
        return redisTemplate.opsForValue()
                .set(key, value, ttl)
                .onErrorReturn(false);
    }

    @Override
    public Mono<Boolean> delete(String key) {
        return redisTemplate.delete(key)
                .map(count -> count > 0)
                .onErrorReturn(false);
    }
}
