package org.tavall.control.healing;

import org.tavall.control.asset.GlobalAssetId;
import org.tavall.control.identity.UniversalPlayerId;

import java.time.Instant;
import java.util.Map;

public final class HealingItemCraftingHandler implements HealingDomain {
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
