package com.yennyfer.franchises_api.controller;

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

import com.yennyfer.franchises_api.dto.UpdateProductRequest;
import com.yennyfer.franchises_api.dto.UpdateProductStockRequest;
import com.yennyfer.franchises_api.model.Product;
import com.yennyfer.franchises_api.service.ProductsService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = ProductsController.class)
class ProductsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ProductsService productsService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("Producto 1")
                .description("Desc")
                .stock(10)
                .sku(1001)
                .branchId(10L)
                .build();
    }

    @Test
    @DisplayName("given product payload when create then returns resource")
    void createProduct_success() {
        when(productsService.createProduct(any(Product.class))).thenReturn(Mono.just(product));

        webTestClient.post()
                .uri("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(product)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(product.getId())
                .jsonPath("$.name").isEqualTo(product.getName());
    }

    @Test
    @DisplayName("given duplicated product when create then returns 409")
    void createProduct_conflict() {
        when(productsService.createProduct(any(Product.class)))
                .thenReturn(Mono.error(new org.springframework.web.server.ResponseStatusException(HttpStatus.CONFLICT, "duplicado")));

        webTestClient.post()
                .uri("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(product)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("when listing products then returns payload")
    void getAllProducts_success() {
        when(productsService.getAllProducts()).thenReturn(Flux.just(product));

        webTestClient.get()
                .uri("/api/products")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(product.getId())
                .jsonPath("$[0].name").isEqualTo(product.getName());
    }

    @Test
    @DisplayName("when listing products fails then returns 500")
    void getAllProducts_error() {
        when(productsService.getAllProducts())
                .thenReturn(Flux.error(new RuntimeException("db error")));

        webTestClient.get()
                .uri("/api/products")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("when updating product then returns updated resource")
    void updateProduct_success() {
        UpdateProductRequest request = new UpdateProductRequest("Nuevo", 2001, "nueva desc");
        Product updated = Product.builder().id(1L).name("Nuevo").sku(2001).description("nueva desc").branchId(10L).stock(5).build();
        when(productsService.updateProduct(eq(1L), any(UpdateProductRequest.class))).thenReturn(Mono.just(updated));

        webTestClient.patch()
                .uri("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Nuevo")
                .jsonPath("$.sku").isEqualTo(2001);
    }

    @Test
    @DisplayName("when updating product not found then returns 404")
    void updateProduct_notFound() {
        when(productsService.updateProduct(eq(1L), any(UpdateProductRequest.class)))
                .thenReturn(Mono.error(new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "no existe")));

        webTestClient.patch()
                .uri("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UpdateProductRequest("Nuevo", 2001, "nueva desc"))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("when updating stock then returns updated product")
    void updateProductStock_success() {
        UpdateProductStockRequest request = new UpdateProductStockRequest(25);
        Product updated = Product.builder().id(1L).name("Producto 1").stock(25).build();
        when(productsService.updateProductStock(eq(1L), any(UpdateProductStockRequest.class))).thenReturn(Mono.just(updated));

        webTestClient.patch()
                .uri("/api/products/1/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.stock").isEqualTo(25);
    }

    @Test
    @DisplayName("when updating stock not found then returns 404")
    void updateProductStock_notFound() {
        when(productsService.updateProductStock(eq(1L), any(UpdateProductStockRequest.class)))
                .thenReturn(Mono.error(new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "no existe")));

        webTestClient.patch()
                .uri("/api/products/1/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UpdateProductStockRequest(25))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("when deleting product then returns empty body")
    void deleteProduct_success() {
        when(productsService.deleteProduct(1L)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/products/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();
    }

    @Test
    @DisplayName("when deleting product missing then returns 404")
    void deleteProduct_notFound() {
        when(productsService.deleteProduct(1L))
                .thenReturn(Mono.error(new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "no existe")));

        webTestClient.delete()
                .uri("/api/products/1")
                .exchange()
                .expectStatus().isNotFound();
    }
}
