package com.yennyfer.franchises_api.service;

import com.yennyfer.franchises_api.model.Branch;
import com.yennyfer.franchises_api.model.BranchAggregate;
import com.yennyfer.franchises_api.model.Franchise;
import com.yennyfer.franchises_api.model.FranchiseAggregate;
import com.yennyfer.franchises_api.repository.BranchRepository;
import com.yennyfer.franchises_api.repository.FranchisesRepository;
import com.yennyfer.franchises_api.repository.ProductsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class FranchisesService {
    private final FranchisesRepository franchisesRepository;
    private final BranchRepository branchRepository;
    private final ProductsRepository productsRepository;

    public Mono<Franchise> createFranchise(Franchise franchise) {
        return franchisesRepository.save(franchise);
    }

    public Flux<Franchise> getAllFranchises() {
        return franchisesRepository.findAll();
    }

    public Flux<FranchiseAggregate> getFranchisesWithDetails() {
        return franchisesRepository.findAll()
                .flatMap(this::mapFranchiseToAggregate);
    }

    private Mono<FranchiseAggregate> mapFranchiseToAggregate(Franchise franchise) {
        return branchRepository.findByFranchiseId(franchise.getId())
                .flatMap(this::mapBranchToAggregate)
                .collectList()
                .map(branches -> new FranchiseAggregate(franchise, branches));
    }

    private Mono<BranchAggregate> mapBranchToAggregate(Branch branch) {
        return productsRepository.findByBranchId(branch.getId())
                .collectList()
                .map(products -> new BranchAggregate(branch, products));
    }
}
