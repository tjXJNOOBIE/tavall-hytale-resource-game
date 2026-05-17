package com.tavall.resourcegame.middleware.healing;

import com.tavall.resourcegame.middleware.asset.GlobalAssetId;
import com.tavall.resourcegame.middleware.identity.UniversalPlayerId;

import java.time.Instant;
import java.util.Map;

public final class HealingItemCraftingHandler implements IHealingDomain {
    public HealingItemCraftingHandler() {
    }

    public HealingItemCraftingHandler(MedicalItemCraftingHandler medicalItemCraftingHandler) {
        registerMedicalItemCraftingHandler(medicalItemCraftingHandler);
    }

    public HealingInventory craftHealingItem(UniversalPlayerId universalPlayerId, HealingItemType healingItemType, int quantity, Instant now) {
        return getMedicalItemCraftingHandler().craftHealingItem(universalPlayerId, healingItemType, quantity, now);
    }

    public Map<GlobalAssetId, Integer> craftingRecipeFor(HealingItemType healingItemType) {
        return getMedicalItemCraftingHandler().craftingRecipeFor(healingItemType);
    }
}
