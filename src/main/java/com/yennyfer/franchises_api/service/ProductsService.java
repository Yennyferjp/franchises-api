package com.yennyfer.franchises_api.service;

import com.yennyfer.franchises_api.dto.ProductMaxStockResponse;
import com.yennyfer.franchises_api.dto.UpdateProductRequest;
import com.yennyfer.franchises_api.dto.UpdateProductStockRequest;
import com.yennyfer.franchises_api.model.Product;
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
public class ProductsService {

    private final ProductsRepository productsRepository;

    public Mono<Product> createProduct(Product product) {
        return productsRepository.findByName(product.getName()).collectList().flatMap(existingProducts -> {
            if (!existingProducts.isEmpty()) {
                return Mono.error(new IllegalArgumentException("El producto con el nombre proporcionado ya existe."));
            }
            return productsRepository.save(product);
        });
    }

    public Flux<Product> getAllProducts() {
        return productsRepository.findAll();
    }

    public Mono<Void> deleteProduct(Long productId) {
        return productsRepository.existsById(productId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "El producto no existe"
                                )
                        );
                    }
                    return productsRepository.deleteById(productId);
                });
    }

    public Mono<Product> updateProductStock(Long productId, UpdateProductStockRequest request) {
        if (request == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "El cuerpo de la petición es obligatorio"));
        }

        return productsRepository.findById(productId).switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"))).flatMap(product -> {
            product.setStock(request.stock());
            return productsRepository.save(product);
        });
    }

    public Mono<Product> updateProduct(
            Long productId,
            @Valid UpdateProductRequest request) {
        if (request == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "El cuerpo de la petición es obligatorio"));
        }
        return productsRepository.findById(productId).switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"))).flatMap(product -> {
            product.setName(request.name());
            product.setDescription(request.description());
            product.setSku(request.sku());
            return productsRepository.save(product);
        });
    }

    public Flux<ProductMaxStockResponse> getProductsWithMaxStockPerFranchise(Long franchiseId) {
        return productsRepository.findProductsWithMaxStockPerFranchise(franchiseId);
    }
}
