package com.yennyfer.franchises_api.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateProductStockRequest(
        @NotNull Integer stock
){}

