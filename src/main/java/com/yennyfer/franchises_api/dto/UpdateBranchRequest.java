package com.yennyfer.franchises_api.dto;

public record UpdateBranchRequest(
        String name,
        String address,
        Long franchiseId
) {}
