package com.yennyfer.franchises_api.repository;

import com.yennyfer.franchises_api.model.Product;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ProductsRepository extends ReactiveCrudRepository<Product, Long> {

    Flux<Product> findByBranchId(Long branchId);
    Flux<Product> findByName(String name);
}
