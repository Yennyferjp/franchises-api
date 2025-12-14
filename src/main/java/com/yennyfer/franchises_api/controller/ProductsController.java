package com.yennyfer.franchises_api.controller;

import com.yennyfer.franchises_api.model.Product;
import com.yennyfer.franchises_api.service.ProductsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/products")
@RequiredArgsConstructor
public class ProductsController {
    private final ProductsService productsService;

    @PostMapping
    public Mono<Product> create(@RequestBody Product product){
        return productsService.createProduct(product);
    }

    @GetMapping
    public Flux<Product> getAll(){
        return productsService.getAllProducts();
    }

    @DeleteMapping("api/products/{productId}")
    public Mono<Void> deleteProduct(@PathVariable Long productId) {
        return productsService.deleteProduct(productId);
    }
}
