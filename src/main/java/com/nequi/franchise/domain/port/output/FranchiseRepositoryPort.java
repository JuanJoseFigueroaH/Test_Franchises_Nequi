package com.nequi.franchise.domain.port.output;

import com.nequi.franchise.domain.model.Franchise;
import com.nequi.franchise.domain.model.Page;
import reactor.core.publisher.Mono;

public interface FranchiseRepositoryPort {
    Mono<Franchise> save(Franchise franchise);
    Mono<Franchise> findById(String id);
    Mono<Void> delete(String id);
    Mono<Page<Franchise>> findAll(Integer pageSize, String cursor);
}
