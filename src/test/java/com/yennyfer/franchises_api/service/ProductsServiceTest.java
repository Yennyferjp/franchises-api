package com.yennyfer.franchises_api.service;

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

import com.yennyfer.franchises_api.dto.ProductMaxStockResponse;
import com.yennyfer.franchises_api.dto.UpdateProductRequest;
import com.yennyfer.franchises_api.dto.UpdateProductStockRequest;
import com.yennyfer.franchises_api.model.Product;
import com.yennyfer.franchises_api.repository.ProductsRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ProductsServiceTest {

    @Mock
    private ProductsRepository productsRepository;

    @InjectMocks
    private ProductsService productsService;

    private Product product;

    @BeforeEach
    void init() {
        product = Product.builder().id(1L).name("Producto").stock(10).sku(1001).branchId(10L).build();
    }

    @Test
    @DisplayName("given unique product when create then saves entity")
    void createProduct_success() {
        when(productsRepository.findByName(product.getName())).thenReturn(Flux.empty());
        when(productsRepository.save(product)).thenReturn(Mono.just(product));

        StepVerifier.create(productsService.createProduct(product))
                .expectNext(product)
                .verifyComplete();
    }

    @Test
    @DisplayName("given duplicated product name when create then emits error")
    void createProduct_conflict() {
        when(productsRepository.findByName(product.getName())).thenReturn(Flux.just(product));

        StepVerifier.create(productsService.createProduct(product))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    @DisplayName("when listing products then returns flux")
    void getAllProducts() {
        when(productsRepository.findAll()).thenReturn(Flux.just(product));

        StepVerifier.create(productsService.getAllProducts())
                .expectNext(product)
                .verifyComplete();
    }

    @Test
    @DisplayName("when deleting product exists then completes")
    void deleteProduct_success() {
        when(productsRepository.existsById(1L)).thenReturn(Mono.just(true));
        when(productsRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(productsService.deleteProduct(1L))
                .verifyComplete();
    }

    @Test
    @DisplayName("when deleting product missing then not found")
    void deleteProduct_notFound() {
        when(productsRepository.existsById(1L)).thenReturn(Mono.just(false));

        StepVerifier.create(productsService.deleteProduct(1L))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("given request when update product then persists changes")
    void updateProduct_success() {
        UpdateProductRequest request = new UpdateProductRequest("Nuevo", 2001, "desc");
        Product updated = Product.builder().id(1L).name("Nuevo").sku(2001).description("desc").branchId(10L).stock(10).build();
        when(productsRepository.findById(1L)).thenReturn(Mono.just(product));
        when(productsRepository.save(any(Product.class))).thenReturn(Mono.just(updated));

        StepVerifier.create(productsService.updateProduct(1L, request))
                .expectNext(updated)
                .verifyComplete();
    }

    @Test
    @DisplayName("given unknown product when update then emits not found")
    void updateProduct_notFound() {
        when(productsRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(productsService.updateProduct(1L, new UpdateProductRequest("Nuevo", 2001, "desc")))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("given request when update stock then saves value")
    void updateProductStock_success() {
        UpdateProductStockRequest request = new UpdateProductStockRequest(25);
        Product updated = Product.builder().id(1L).name("Producto").stock(25).build();
        when(productsRepository.findById(1L)).thenReturn(Mono.just(product));
        when(productsRepository.save(any(Product.class))).thenReturn(Mono.just(updated));

        StepVerifier.create(productsService.updateProductStock(1L, request))
                .expectNext(updated)
                .verifyComplete();
    }

    @Test
    @DisplayName("given missing product when update stock then emits not found")
    void updateProductStock_notFound() {
        when(productsRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(productsService.updateProductStock(1L, new UpdateProductStockRequest(10)))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("when querying max stock then delegates to repository")
    void getProductsWithMaxStockPerFranchise() {
        ProductMaxStockResponse response = new ProductMaxStockResponse(10L, "Sucursal", 1L, "Producto", 50);
        when(productsRepository.findProductsWithMaxStockPerFranchise(1L)).thenReturn(Flux.just(response));

        StepVerifier.create(productsService.getProductsWithMaxStockPerFranchise(1L))
                .expectNext(response)
                .verifyComplete();
    }
}

