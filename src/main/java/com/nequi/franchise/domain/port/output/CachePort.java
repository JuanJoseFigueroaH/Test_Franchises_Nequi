package com.nequi.franchise.domain.port.output;

import reactor.core.publisher.Mono;
import java.time.Duration;

public interface CachePort {
    <T> Mono<T> get(String key, Class<T> type);
    <T> Mono<Boolean> set(String key, T value, Duration ttl);
    Mono<Boolean> delete(String key);
}
