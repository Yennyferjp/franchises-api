package com.yennyfer.franchises_api.service;

import com.yennyfer.franchises_api.model.Product;
import com.yennyfer.franchises_api.repository.ProductsRepository;
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
        return productsRepository.save(product);
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
}
