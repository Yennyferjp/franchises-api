package com.yennyfer.franchises_api.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.yennyfer.franchises_api.dto.UpdateBranchRequest;
import com.yennyfer.franchises_api.model.Branch;
import com.yennyfer.franchises_api.model.Product;
import com.yennyfer.franchises_api.repository.BranchRepository;
import com.yennyfer.franchises_api.repository.ProductsRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class BranchesServiceTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private ProductsRepository productsRepository;

    @InjectMocks
    private BranchesService branchesService;

    private Branch branch;

    @BeforeEach
    void init() {
        branch = Branch.builder().id(10L).name("Sucursal Centro").address("Av. Principal 123").franchiseId(1L).build();
    }

    @Test
    @DisplayName("given unique name when create branch then succeeds")
    void createBranch_success() {
        when(branchRepository.findByName("Sucursal Centro")).thenReturn(Flux.empty());
        when(branchRepository.save(branch)).thenReturn(Mono.just(branch));

        StepVerifier.create(branchesService.createBranch(branch))
                .expectNext(branch)
                .verifyComplete();
    }

    @Test
    @DisplayName("given duplicated name when create branch then emits error")
    void createBranch_conflict() {
        when(branchRepository.findByName("Sucursal Centro")).thenReturn(Flux.just(branch));

        StepVerifier.create(branchesService.createBranch(branch))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    @DisplayName("when listing branches then returns flux")
    void getAllBranches_success() {
        when(branchRepository.findAll()).thenReturn(Flux.just(branch));

        StepVerifier.create(branchesService.getAllBranches())
                .expectNext(branch)
                .verifyComplete();
    }

    @Test
    @DisplayName("when fetching details then returns aggregates")
    void getAllBranchesWithDetails() {
        Product product = Product.builder().id(20L).branchId(10L).name("Combo").build();
        when(branchRepository.findAll()).thenReturn(Flux.just(branch));
        when(productsRepository.findByBranchId(10L)).thenReturn(Flux.just(product));

        StepVerifier.create(branchesService.getAllBranchesWithDetails())
                .assertNext(aggregate -> {
                    assertEquals(branch.getId(), aggregate.branch().getId());
                    List<Product> products = aggregate.products();
                    assertEquals(1, products.size());
                    assertEquals(product.getId(), products.get(0).getId());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("given payload when update branch then persists changes")
    void updateBranch_success() {
        UpdateBranchRequest request = new UpdateBranchRequest("Nueva", "Dir", 2L);
        Branch updated = Branch.builder().id(10L).name("Nueva").address("Dir").franchiseId(2L).build();
        when(branchRepository.findById(10L)).thenReturn(Mono.just(branch));
        when(branchRepository.save(any(Branch.class))).thenReturn(Mono.just(updated));

        StepVerifier.create(branchesService.updateBranch(10L, request))
                .expectNext(updated)
                .verifyComplete();
    }

    @Test
    @DisplayName("given unknown branch when update then emits not found")
    void updateBranch_notFound() {
        when(branchRepository.findById(10L)).thenReturn(Mono.empty());

        StepVerifier.create(branchesService.updateBranch(10L, new UpdateBranchRequest("Nueva", "Dir", 2L)))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("given existing id when delete branch then completes")
    void deleteBranch_success() {
        when(branchRepository.existsById(10L)).thenReturn(Mono.just(true));
        when(branchRepository.deleteById(10L)).thenReturn(Mono.empty());

        StepVerifier.create(branchesService.deleteBranch(10L))
                .verifyComplete();
    }

    @Test
    @DisplayName("given missing id when delete branch then emits not found")
    void deleteBranch_notFound() {
        when(branchRepository.existsById(99L)).thenReturn(Mono.just(false));

        StepVerifier.create(branchesService.deleteBranch(99L))
                .expectError(ResponseStatusException.class)
                .verify();
    }
}

