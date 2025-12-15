package com.yennyfer.franchises_api.repository;

import com.yennyfer.franchises_api.model.Franchise;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface FranchisesRepository extends ReactiveCrudRepository<Franchise, Long> {
    Flux<Franchise> findByName(String name);
}
