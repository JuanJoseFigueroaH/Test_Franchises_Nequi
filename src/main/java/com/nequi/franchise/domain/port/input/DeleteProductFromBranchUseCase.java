package com.nequi.franchise.domain.port.input;

import com.nequi.franchise.domain.model.Franchise;
import reactor.core.publisher.Mono;

public interface DeleteProductFromBranchUseCase {
    Mono<Franchise> execute(String franchiseId, String branchId, String productId);
}
