package com.yennyfer.franchises_api.service;

import com.yennyfer.franchises_api.model.Branch;
import com.yennyfer.franchises_api.model.BranchAggregate;
import com.yennyfer.franchises_api.repository.BranchRepository;
import com.yennyfer.franchises_api.repository.ProductsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BranchesService {

    private final BranchRepository branchRepository;
    private final ProductsRepository productsRepository;

    public Mono<Branch> createBranch(Branch branch){
        return branchRepository.save(branch);
    }

    public Flux<Branch> getAllBranches(){
        return branchRepository.findAll();
    }

    public Flux<BranchAggregate> getAllBranchesWithDetails() {
        return branchRepository.findAll().flatMap(this::mapBranchToAggregate);
    }

    private Mono<BranchAggregate> mapBranchToAggregate(Branch branch) {
        return productsRepository.findByBranchId(branch.getId())
                .collectList()
                .map(products -> new BranchAggregate(branch, products));
    }
}
