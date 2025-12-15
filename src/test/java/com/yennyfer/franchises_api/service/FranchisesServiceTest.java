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

import com.yennyfer.franchises_api.dto.UpdateFranchiseRequest;
import com.yennyfer.franchises_api.model.Branch;
import com.yennyfer.franchises_api.model.BranchAggregate;
import com.yennyfer.franchises_api.model.Franchise;
import com.yennyfer.franchises_api.model.Product;
import com.yennyfer.franchises_api.repository.BranchRepository;
import com.yennyfer.franchises_api.repository.FranchisesRepository;
import com.yennyfer.franchises_api.repository.ProductsRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class FranchisesServiceTest {

    @Mock
    private FranchisesRepository franchisesRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private ProductsRepository productsRepository;

    @InjectMocks
    private FranchisesService franchisesService;

    private Franchise franchise;

    @BeforeEach
    void init() {
        franchise = Franchise.builder().id(1L).name("Franquicia Norte").build();
    }

    @Test
    @DisplayName("given unique name when create franchise then returns saved entity")
    void createFranchise_success() {
        when(franchisesRepository.findByName("Franquicia Norte")).thenReturn(Flux.empty());
        when(franchisesRepository.save(franchise)).thenReturn(Mono.just(franchise));

        StepVerifier.create(franchisesService.createFranchise(franchise))
                .expectNext(franchise)
                .verifyComplete();
    }

    @Test
    @DisplayName("given duplicated name when create franchise then emits error")
    void createFranchise_conflict() {
        when(franchisesRepository.findByName("Franquicia Norte")).thenReturn(Flux.just(franchise));

        StepVerifier.create(franchisesService.createFranchise(franchise))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    @DisplayName("when listing franchises then returns flux")
    void getAllFranchises() {
        when(franchisesRepository.findAll()).thenReturn(Flux.just(franchise));

        StepVerifier.create(franchisesService.getAllFranchises())
                .expectNext(franchise)
                .verifyComplete();
    }

    @Test
    @DisplayName("when fetching aggregates then builds nested response")
    void getFranchisesWithDetails() {
        Branch branch = Branch.builder().id(10L).franchiseId(1L).name("Sucursal Centro").build();
        Product product = Product.builder().id(20L).branchId(10L).name("Combo").build();

        when(franchisesRepository.findAll()).thenReturn(Flux.just(franchise));
        when(branchRepository.findByFranchiseId(1L)).thenReturn(Flux.just(branch));
        when(productsRepository.findByBranchId(10L)).thenReturn(Flux.just(product));

        StepVerifier.create(franchisesService.getFranchisesWithDetails())
                .assertNext(aggregate -> {
                    assertEquals(franchise.getId(), aggregate.franchise().getId());
                    List<BranchAggregate> branches = aggregate.branches();
                    assertEquals(1, branches.size());
                    assertEquals(branch.getId(), branches.get(0).branch().getId());
                    assertEquals(1, branches.get(0).products().size());
                    assertEquals(product.getId(), branches.get(0).products().get(0).getId());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("given payload when update franchise then persists changes")
    void updateFranchise_success() {
        UpdateFranchiseRequest request = new UpdateFranchiseRequest("Nueva");
        Franchise updated = Franchise.builder().id(1L).name("Nueva").build();
        when(franchisesRepository.findById(1L)).thenReturn(Mono.just(franchise));
        when(franchisesRepository.save(any(Franchise.class))).thenReturn(Mono.just(updated));

        StepVerifier.create(franchisesService.updateFranchise(1L, request))
                .expectNext(updated)
                .verifyComplete();
    }

    @Test
    @DisplayName("given unknown franchise when update then returns not found")
    void updateFranchise_notFound() {
        when(franchisesRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(franchisesService.updateFranchise(99L, new UpdateFranchiseRequest("Nueva")))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("given existing id when delete franchise then completes")
    void deleteFranchise_success() {
        when(franchisesRepository.existsById(1L)).thenReturn(Mono.just(true));
        when(franchisesRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(franchisesService.deleteFranchise(1L))
                .verifyComplete();
    }

    @Test
    @DisplayName("given missing id when delete franchise then emits not found")
    void deleteFranchise_notFound() {
        when(franchisesRepository.existsById(99L)).thenReturn(Mono.just(false));

        StepVerifier.create(franchisesService.deleteFranchise(99L))
                .expectError(ResponseStatusException.class)
                .verify();
    }
}

