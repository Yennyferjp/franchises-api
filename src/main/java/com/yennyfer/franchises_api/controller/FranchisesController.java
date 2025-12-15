package com.yennyfer.franchises_api.controller;

import com.yennyfer.franchises_api.dto.ProductMaxStockResponse;
import com.yennyfer.franchises_api.dto.UpdateFranchiseRequest;
import com.yennyfer.franchises_api.model.Franchise;
import com.yennyfer.franchises_api.model.FranchiseAggregate;
import com.yennyfer.franchises_api.service.FranchisesService;
import com.yennyfer.franchises_api.service.ProductsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/franchises")
@RequiredArgsConstructor
public class FranchisesController {
    private final FranchisesService franchisesService;
    private final ProductsService productService;

    @PostMapping
    public Mono<Franchise> create(@RequestBody Franchise franchise){
        return franchisesService.createFranchise(franchise);
    }

    @GetMapping
    public Flux<Franchise> getAll(){
        return franchisesService.getAllFranchises();
    }

    @GetMapping("/details")
    public Flux<FranchiseAggregate> getAllWithDetails() {
        return franchisesService.getFranchisesWithDetails();
    }

    @DeleteMapping("{franchiseId}")
    public Mono<Void> deleteFranchise(
            @PathVariable Long franchiseId) {
        return franchisesService.deleteFranchise(franchiseId);
    }

    @PatchMapping("{franchiseId}")
    public Mono<Franchise> updateFranchise(
            @PathVariable Long franchiseId,
            @Valid @RequestBody UpdateFranchiseRequest request) {
        return franchisesService.updateFranchise(franchiseId, request);
    }

    @GetMapping("/{franchiseId}/products/max-stock")
    public Flux<ProductMaxStockResponse> getProductsWithMaxStockPerFranchise(
            @PathVariable Long franchiseId) {
        return productService.getProductsWithMaxStockPerFranchise(franchiseId);
    }
}
