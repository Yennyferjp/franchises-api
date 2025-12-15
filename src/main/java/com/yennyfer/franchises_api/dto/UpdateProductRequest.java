package com.yennyfer.franchises_api.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateProductRequest(
        @NotNull String name,
        @NotNull Integer sku,
        String description
) {}
