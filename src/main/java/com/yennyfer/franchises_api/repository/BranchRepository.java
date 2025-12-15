package com.yennyfer.franchises_api.repository;

import com.yennyfer.franchises_api.model.Branch;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface BranchRepository extends ReactiveCrudRepository<Branch, Long> {

    Flux<Branch> findByFranchiseId(Long franchiseId);
    Flux<Branch> findByName(String name);
}
