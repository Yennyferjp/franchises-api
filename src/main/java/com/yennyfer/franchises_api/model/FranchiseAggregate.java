package com.yennyfer.franchises_api.model;

import java.util.List;

public record FranchiseAggregate(
        Franchise franchise,
        List<BranchAggregate> branches) {
}

