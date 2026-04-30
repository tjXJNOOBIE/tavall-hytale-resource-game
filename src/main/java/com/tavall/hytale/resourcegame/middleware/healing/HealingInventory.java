package com.tavall.hytale.resourcegame.middleware.healing;

import com.tavall.hytale.resourcegame.middleware.asset.GlobalAssetId;

import java.util.HashMap;
import java.util.Map;

public record HealingInventory(Map<GlobalAssetId, Integer> amountsByAssetId) {
    public HealingInventory {
        HashMap<GlobalAssetId, Integer> copy = new HashMap<>();
        if (amountsByAssetId != null) {
            for (Map.Entry<GlobalAssetId, Integer> entry : amountsByAssetId.entrySet()) {
                copy.put(entry.getKey(), Math.max(0, entry.getValue() == null ? 0 : entry.getValue()));
            }
        }
        amountsByAssetId = Map.copyOf(copy);
    }

    public int amount(GlobalAssetId globalAssetId) {
        return amountsByAssetId.getOrDefault(globalAssetId, 0);
    }

    public HealingInventory withAdded(GlobalAssetId globalAssetId, int amount) {
        HashMap<GlobalAssetId, Integer> updated = new HashMap<>(amountsByAssetId);
        updated.put(globalAssetId, amount(globalAssetId) + Math.max(0, amount));
        return new HealingInventory(updated);
    }

    public HealingInventory withConsumed(Map<GlobalAssetId, Integer> requiredAmounts) {
        HashMap<GlobalAssetId, Integer> updated = new HashMap<>(amountsByAssetId);
        for (Map.Entry<GlobalAssetId, Integer> entry : requiredAmounts.entrySet()) {
            int available = updated.getOrDefault(entry.getKey(), 0);
            int required = Math.max(0, entry.getValue());
            if (available < required) {
                throw new HealingValidationException("Missing resource " + entry.getKey().value() + ".");
            }
            updated.put(entry.getKey(), available - required);
        }
        return new HealingInventory(updated);
    }
}
