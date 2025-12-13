package com.yennyfer.franchises_api.model;

import java.util.List;

public record BranchAggregate(
        Branch branch,
        List<Product> products) {
}

