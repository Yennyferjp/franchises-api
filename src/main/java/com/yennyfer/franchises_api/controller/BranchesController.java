package com.yennyfer.franchises_api.controller;

import com.yennyfer.franchises_api.model.Branch;
import com.yennyfer.franchises_api.model.BranchAggregate;
import com.yennyfer.franchises_api.service.BranchesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/branches")
@RequiredArgsConstructor
public class BranchesController {
    private final BranchesService branchesService;

    @PostMapping
    public Mono<Branch> create(@RequestBody Branch branch){
        return branchesService.createBranch(branch);
    }

    @GetMapping
    public Flux<Branch> getAll(){
        return branchesService.getAllBranches();
    }

    @GetMapping("/details")
    public Flux<BranchAggregate> getAllWithDetails() {
        return branchesService.getAllBranchesWithDetails();
    }
}
