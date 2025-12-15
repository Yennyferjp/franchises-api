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

import com.yennyfer.franchises_api.dto.ProductMaxStockResponse;
import com.yennyfer.franchises_api.dto.UpdateFranchiseRequest;
import com.yennyfer.franchises_api.model.Branch;
import com.yennyfer.franchises_api.model.BranchAggregate;
import com.yennyfer.franchises_api.model.Franchise;
import com.yennyfer.franchises_api.model.FranchiseAggregate;
import com.yennyfer.franchises_api.model.Product;
import com.yennyfer.franchises_api.service.FranchisesService;
import com.yennyfer.franchises_api.service.ProductsService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = FranchisesController.class)
class FranchisesControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private FranchisesService franchisesService;

    @MockitoBean
    private ProductsService productsService;

    private Franchise franchise;

    @BeforeEach
    void setUp() {
        franchise = Franchise.builder()
                .id(1L)
                .name("Franquicia Norte")
                .build();
    }

    @Test
    @DisplayName("given valid franchise when create then returns created resource")
    void createFranchise_success() {
        when(franchisesService.createFranchise(any(Franchise.class)))
                .thenReturn(Mono.just(franchise));

        webTestClient.post()
                .uri("/api/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(franchise)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(franchise.getId())
                .jsonPath("$.name").isEqualTo(franchise.getName());
    }

    @Test
    @DisplayName("given duplicated franchise when create then returns 409")
    void createFranchise_conflict() {
        when(franchisesService.createFranchise(any(Franchise.class)))
                .thenReturn(Mono.error(new org.springframework.web.server.ResponseStatusException(HttpStatus.CONFLICT, "duplicada")));

        webTestClient.post()
                .uri("/api/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(franchise)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("when listing franchises then returns payload")
    void getAllFranchises_success() {
        when(franchisesService.getAllFranchises()).thenReturn(Flux.just(franchise));

        webTestClient.get()
                .uri("/api/franchises")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(franchise.getId())
                .jsonPath("$[0].name").isEqualTo(franchise.getName());
    }

    @Test
    @DisplayName("when listing franchises and service fails then returns 500")
    void getAllFranchises_serverError() {
        when(franchisesService.getAllFranchises())
                .thenReturn(Flux.error(new RuntimeException("db down")));

        webTestClient.get()
                .uri("/api/franchises")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("when requesting franchise details then returns aggregates")
    void getFranchisesWithDetails_success() {
        Branch branch = Branch.builder().id(10L).name("Sucursal Centro").address("Av. Principal 123").franchiseId(1L).build();
        Product product = Product.builder().id(20L).name("Combo").branchId(10L).build();
        FranchiseAggregate aggregate = new FranchiseAggregate(
                franchise,
                List.of(new BranchAggregate(branch, List.of(product)))
        );
        when(franchisesService.getFranchisesWithDetails()).thenReturn(Flux.just(aggregate));

        webTestClient.get()
                .uri("/api/franchises/details")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].franchise.id").isEqualTo(franchise.getId())
                .jsonPath("$[0].branches[0].branch.id").isEqualTo(branch.getId())
                .jsonPath("$[0].branches[0].products[0].id").isEqualTo(product.getId());
    }

    @Test
    @DisplayName("when updating franchise then returns updated entity")
    void updateFranchise_success() {
        UpdateFranchiseRequest request = new UpdateFranchiseRequest("Nueva");
        Franchise updated = Franchise.builder().id(1L).name("Nueva").build();
        when(franchisesService.updateFranchise(eq(1L), any(UpdateFranchiseRequest.class)))
                .thenReturn(Mono.just(updated));

        webTestClient.patch()
                .uri("/api/franchises/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Nueva");
    }

    @Test
    @DisplayName("when updating franchise not found then returns 404")
    void updateFranchise_notFound() {
        when(franchisesService.updateFranchise(eq(99L), any(UpdateFranchiseRequest.class)))
                .thenReturn(Mono.error(new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "no existe")));

        webTestClient.patch()
                .uri("/api/franchises/99")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UpdateFranchiseRequest("Nueva"))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("when deleting franchise then returns empty body")
    void deleteFranchise_success() {
        when(franchisesService.deleteFranchise(1L)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/franchises/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();
    }

    @Test
    @DisplayName("when deleting franchise not found then returns 404")
    void deleteFranchise_notFound() {
        when(franchisesService.deleteFranchise(99L))
                .thenReturn(Mono.error(new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "no existe")));

        webTestClient.delete()
                .uri("/api/franchises/99")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("when querying products with max stock then returns payload")
    void getProductsWithMaxStock_success() {
        ProductMaxStockResponse response = new ProductMaxStockResponse(10L, "Sucursal Centro", 20L, "Combo", 80);
        when(productsService.getProductsWithMaxStockPerFranchise(1L))
                .thenReturn(Flux.just(response));

        webTestClient.get()
                .uri("/api/franchises/1/products/max-stock")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductMaxStockResponse.class)
                .hasSize(1)
                .contains(response);
    }

    @Test
    @DisplayName("when max stock query has no franchise then returns 404")
    void getProductsWithMaxStock_notFound() {
        when(productsService.getProductsWithMaxStockPerFranchise(99L))
                .thenReturn(Flux.error(new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "sin datos")));

        webTestClient.get()
                .uri("/api/franchises/99/products/max-stock")
                .exchange()
                .expectStatus().isNotFound();
    }
}
