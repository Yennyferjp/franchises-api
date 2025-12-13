package com.yennyfer.franchises_api.controller;

import com.yennyfer.franchises_api.model.Franchise;
import com.yennyfer.franchises_api.model.FranchiseAggregate;
import com.yennyfer.franchises_api.service.FranchisesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/franchises")
@RequiredArgsConstructor
public class FranchisesController {
    private final FranchisesService franchisesService;

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
}
