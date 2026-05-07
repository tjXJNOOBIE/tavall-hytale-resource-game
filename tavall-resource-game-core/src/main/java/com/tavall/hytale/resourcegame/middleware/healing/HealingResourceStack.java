package com.tavall.hytale.resourcegame.middleware.healing;

import com.tavall.hytale.resourcegame.middleware.asset.GlobalAssetId;

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
