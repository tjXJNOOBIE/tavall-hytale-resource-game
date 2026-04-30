package com.tavall.hytale.resourcegame.middleware.healing;

import com.tavall.hytale.resourcegame.middleware.asset.GlobalAssetId;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;

import java.time.Instant;
import java.util.Map;

public final class HealingItemCraftingHandler {
    private final MedicalItemCraftingHandler medicalItemCraftingHandler;

    public HealingItemCraftingHandler(MedicalItemCraftingHandler medicalItemCraftingHandler) {
        this.medicalItemCraftingHandler = medicalItemCraftingHandler;
    }

    public HealingInventory craftHealingItem(UniversalPlayerId universalPlayerId, HealingItemType healingItemType, int quantity, Instant now) {
        return medicalItemCraftingHandler.craftHealingItem(universalPlayerId, healingItemType, quantity, now);
    }

    public Map<GlobalAssetId, Integer> craftingRecipeFor(HealingItemType healingItemType) {
        return medicalItemCraftingHandler.craftingRecipeFor(healingItemType);
    }
}
