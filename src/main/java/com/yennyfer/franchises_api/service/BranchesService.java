package com.yennyfer.franchises_api.service;

import com.yennyfer.franchises_api.dto.UpdateBranchRequest;
import com.yennyfer.franchises_api.model.Branch;
import com.yennyfer.franchises_api.model.BranchAggregate;
import com.yennyfer.franchises_api.repository.BranchRepository;
import com.yennyfer.franchises_api.repository.ProductsRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BranchesService {

    private final BranchRepository branchRepository;
    private final ProductsRepository productsRepository;

    public Mono<Branch> createBranch(Branch branch) {
        return branchRepository.findByName(
                        branch.getName()
                ).collectList()
                .flatMap(existingBranches -> {
                    ;
                    if (!existingBranches.isEmpty()) {
                        return Mono.error(new IllegalArgumentException("La sucursal con el nombre proporcionado ya existe."));
                    }
                    return branchRepository.save(branch);
                });

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


    public Mono<Branch> updateBranch(
            Long branchId,
            @Valid UpdateBranchRequest request) {

        if (request == null) {
            return Mono.error(
                    new IllegalArgumentException("El cuerpo de la petición es obligatorio")
            );
        }
        return branchRepository.findById(branchId)
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Sucursal no encontrada")
                ))
                .flatMap(branch -> {
                    branch.setName(request.name());
                    branch.setAddress(request.address());
                    branch.setFranchiseId(request.franchiseId());
                    return branchRepository.save(branch);
                });
    }

    public Mono<Void> deleteBranch(Long branchId) {
        return branchRepository.existsById(branchId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "La sucursal no existe"
                                )
                        );
                    }
                    return branchRepository.deleteById(branchId);
                });
    }
}
