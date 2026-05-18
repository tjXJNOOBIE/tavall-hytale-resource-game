package org.tavall.control.healing;

import org.tavall.control.asset.GlobalAssetId;

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
