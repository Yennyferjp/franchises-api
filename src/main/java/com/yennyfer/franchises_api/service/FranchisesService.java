package com.yennyfer.franchises_api.service;

import com.yennyfer.franchises_api.dto.UpdateFranchiseRequest;
import com.yennyfer.franchises_api.model.Branch;
import com.yennyfer.franchises_api.model.BranchAggregate;
import com.yennyfer.franchises_api.model.Franchise;
import com.yennyfer.franchises_api.model.FranchiseAggregate;
import com.yennyfer.franchises_api.repository.BranchRepository;
import com.yennyfer.franchises_api.repository.FranchisesRepository;
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
public class FranchisesService {
    private final FranchisesRepository franchisesRepository;
    private final BranchRepository branchRepository;
    private final ProductsRepository productsRepository;

    public Mono<Franchise> createFranchise(Franchise franchise) {
        return franchisesRepository.findByName(
                        franchise.getName()
                ).collectList()
                .flatMap(existingFranchises -> {
                    if (!existingFranchises.isEmpty()) {
                        return Mono.error(new IllegalArgumentException("La franquicia con el nombre proporcionado ya existe."));
                    }
                    return franchisesRepository.save(franchise);
                }
        );
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

    public Mono<Franchise> updateFranchise(
            Long franchiseId,
            @Valid UpdateFranchiseRequest request) {
        if(request == null) {
            return Mono.error(new IllegalArgumentException("El cuerpo de la petición es obligatorio"));
        }
    return franchisesRepository.findById(franchiseId).switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Franquicia no encontrada"))).flatMap(franchise -> {
        franchise.setName(request.name());
        return franchisesRepository.save(franchise);
    });
    }

    public Mono<Void> deleteFranchise(Long franchiseId) {
        return franchisesRepository.existsById(franchiseId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "La franquicia no existe"
                                )
                        );
                    }
                    return franchisesRepository.deleteById(franchiseId);
                });
    }
}
