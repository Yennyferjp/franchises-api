package com.yennyfer.franchises_api.repository;

import com.yennyfer.franchises_api.dto.ProductMaxStockResponse;
import com.yennyfer.franchises_api.model.Product;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ProductsRepository extends ReactiveCrudRepository<Product, Long> {

    Flux<Product> findByBranchId(Long branchId);
    Flux<Product> findByName(String name);

    @Query("""
    SELECT
        p.id AS product_id,
        p.name AS product_name,
        p.stock AS stock,
        b.id AS branch_id,
        b.name AS branch_name
    FROM product p
    JOIN branch b ON p.branch_id = b.id
    WHERE b.franchise_id = :franchiseId
      AND p.stock = (
          SELECT MAX(p2.stock)
          FROM product p2
          WHERE p2.branch_id = b.id
      )
""")
    Flux<ProductMaxStockResponse> findProductsWithMaxStockPerFranchise(Long franchiseId);
}
