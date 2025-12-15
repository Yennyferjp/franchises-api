package com.yennyfer.franchises_api.controller;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.yennyfer.franchises_api.dto.UpdateBranchRequest;
import com.yennyfer.franchises_api.model.Branch;
import com.yennyfer.franchises_api.model.BranchAggregate;
import com.yennyfer.franchises_api.model.Product;
import com.yennyfer.franchises_api.service.BranchesService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = BranchesController.class)
class BranchesControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private BranchesService branchesService;

    private Branch branch;

    @BeforeEach
    void setUp() {
        branch = Branch.builder()
                .id(10L)
                .name("Sucursal Centro")
                .address("Av. Principal 123")
                .franchiseId(1L)
                .build();
    }

    @Test
    @DisplayName("given branch payload when create then returns resource")
    void createBranch_success() {
        when(branchesService.createBranch(any(Branch.class))).thenReturn(Mono.just(branch));

        webTestClient.post()
                .uri("/api/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(branch)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(branch.getId())
                .jsonPath("$.name").isEqualTo(branch.getName());
    }

    @Test
    @DisplayName("given duplicated branch when create then returns 409")
    void createBranch_conflict() {
        when(branchesService.createBranch(any(Branch.class)))
                .thenReturn(Mono.error(new org.springframework.web.server.ResponseStatusException(HttpStatus.CONFLICT, "duplicada")));

        webTestClient.post()
                .uri("/api/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(branch)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("when listing branches then returns collection")
    void getAllBranches_success() {
        when(branchesService.getAllBranches()).thenReturn(Flux.just(branch));

        webTestClient.get()
                .uri("/api/branches")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(branch.getId())
                .jsonPath("$[0].name").isEqualTo(branch.getName());
    }

    @Test
    @DisplayName("when listing branches fails then returns 500")
    void getAllBranches_error() {
        when(branchesService.getAllBranches())
                .thenReturn(Flux.error(new RuntimeException("db error")));

        webTestClient.get()
                .uri("/api/branches")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("when requesting branches with details then returns aggregates")
    void getAllBranchesWithDetails_success() {
        BranchAggregate aggregate = new BranchAggregate(branch, List.of(Product.builder().id(1L).name("Combo").build()));
        when(branchesService.getAllBranchesWithDetails()).thenReturn(Flux.just(aggregate));

        webTestClient.get()
                .uri("/api/branches/details")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].branch.id").isEqualTo(branch.getId())
                .jsonPath("$[0].products[0].name").isEqualTo("Combo");
    }

    @Test
    @DisplayName("when updating branch then returns updated resource")
    void updateBranch_success() {
        UpdateBranchRequest request = new UpdateBranchRequest("Nueva", "Dir", 2L);
        Branch updated = Branch.builder().id(10L).name("Nueva").address("Dir").franchiseId(2L).build();
        when(branchesService.updateBranch(eq(10L), any(UpdateBranchRequest.class))).thenReturn(Mono.just(updated));

        webTestClient.patch()
                .uri("/api/branches/10")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Nueva")
                .jsonPath("$.franchiseId").isEqualTo(2);
    }

    @Test
    @DisplayName("when updating branch not found then returns 404")
    void updateBranch_notFound() {
        when(branchesService.updateBranch(eq(10L), any(UpdateBranchRequest.class)))
                .thenReturn(Mono.error(new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "no existe")));

        webTestClient.patch()
                .uri("/api/branches/10")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UpdateBranchRequest("Nueva", "Dir", 2L))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("when deleting branch then returns empty body")
    void deleteBranch_success() {
        when(branchesService.deleteBranch(10L)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/branches/10")
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();
    }

    @Test
    @DisplayName("when deleting branch missing then returns 404")
    void deleteBranch_notFound() {
        when(branchesService.deleteBranch(99L))
                .thenReturn(Mono.error(new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "no existe")));

        webTestClient.delete()
                .uri("/api/branches/99")
                .exchange()
                .expectStatus().isNotFound();
    }
}
