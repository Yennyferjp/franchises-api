package com.yennyfer.franchises_api.dto;

public record ProductMaxStockResponse(
        Long branchId,
        String branchName,
        Long productId,
        String productName,
        Integer stock
) {}
