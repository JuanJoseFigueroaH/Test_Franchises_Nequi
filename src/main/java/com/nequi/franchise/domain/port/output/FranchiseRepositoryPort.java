package com.nequi.franchise.domain.port.output;

import com.nequi.franchise.domain.model.Franchise;
import reactor.core.publisher.Mono;

public interface FranchiseRepositoryPort {
    Mono<Franchise> save(Franchise franchise);
    Mono<Franchise> findById(String id);
    Mono<Void> delete(String id);
}
