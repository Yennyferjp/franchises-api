package com.yennyfer.franchises_api.repository;

import com.yennyfer.franchises_api.model.Franchise;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface FranchisesRepository extends ReactiveCrudRepository<Franchise, Long> {
}
