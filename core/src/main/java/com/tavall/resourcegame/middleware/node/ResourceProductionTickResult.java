package com.tavall.resourcegame.middleware.node;

public record ResourceProductionTickResult(
        ResourceNode resourceNode,
        int producedAmount
) {
}
