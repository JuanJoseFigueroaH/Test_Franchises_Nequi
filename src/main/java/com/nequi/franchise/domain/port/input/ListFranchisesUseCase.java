package com.nequi.franchise.domain.port.input;

import com.nequi.franchise.domain.model.Franchise;
import com.nequi.franchise.domain.model.Page;
import reactor.core.publisher.Mono;

public interface ListFranchisesUseCase {
    Mono<Page<Franchise>> execute(Integer pageSize, String cursor);
}
