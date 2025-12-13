package com.yennyfer.franchises_api.service;

import com.yennyfer.franchises_api.model.Product;
import com.yennyfer.franchises_api.repository.ProductsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProductsService {

    private final ProductsRepository productsRepository;

    public Mono<Product> createProduct(Product product){
        return productsRepository.save(product);
    }

    public Flux<Product> getAllProducts(){
        return productsRepository.findAll();
    }

}
