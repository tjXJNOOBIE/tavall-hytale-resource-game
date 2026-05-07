package com.tavall.hytale.resourcegame.middleware.node;

public record ResourceProductionTickResult(
        ResourceNode resourceNode,
        int producedAmount
) {
}
