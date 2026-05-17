package com.tavall.resourcegame.middleware.healing;

import com.tavall.resourcegame.middleware.asset.GlobalAssetId;

import java.util.Objects;

public record HealingResourceStack(
        GlobalAssetId globalAssetId,
        int amount
) {
    public HealingResourceStack {
        Objects.requireNonNull(globalAssetId, "globalAssetId");
        amount = Math.max(0, amount);
    }
}
