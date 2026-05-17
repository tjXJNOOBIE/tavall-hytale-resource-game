package com.tavall.resourcegame.middleware.healing;

import com.tavall.resourcegame.middleware.asset.GlobalAssetId;
import com.tavall.resourcegame.middleware.event.DomainEventPublisher;
import com.tavall.resourcegame.middleware.event.SimpleDomainEvent;
import com.tavall.resourcegame.middleware.identity.UniversalPlayerId;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MedicalItemCraftingHandler implements IHealingDomain {
    public MedicalItemCraftingHandler() {
    }

    public MedicalItemCraftingHandler(HealingInventoryRepository healingInventoryRepository, DomainEventPublisher domainEventPublisher) {
        registerHealingInventoryRepository(healingInventoryRepository);
        registerDomainEventPublisher(domainEventPublisher);
    }

    public HealingInventory craftHealingItem(UniversalPlayerId universalPlayerId, HealingItemType healingItemType, int quantity, Instant now) {
        int itemCount = Math.max(1, quantity);
        HealingInventory inventory = getHealingInventoryRepository().findInventory(universalPlayerId)
                .orElse(new HealingInventory(Map.of()));
        Map<GlobalAssetId, Integer> requiredResources = multiply(craftingRecipeFor(healingItemType), itemCount);
        HealingInventory updatedInventory = inventory.withConsumed(requiredResources)
                .withAdded(healingItemType.globalAssetId(), itemCount);
        getHealingInventoryRepository().saveInventory(universalPlayerId, updatedInventory);
        getDomainEventPublisher().publish(new SimpleDomainEvent(
                "HealingItemCraftedEvent",
                now,
                Map.of("universalPlayerId", universalPlayerId.toString(), "healingItem", healingItemType.name(), "quantity", Integer.toString(itemCount))
        ));
        return updatedInventory;
    }

    public Map<GlobalAssetId, Integer> craftingRecipeFor(HealingItemType healingItemType) {
        return switch (healingItemType) {
            case FIELD_RATIONS -> Map.of(
                    MedicalCraftingResource.FOOD_GRAIN.globalAssetId(), 2,
                    MedicalCraftingResource.CLEAN_WATER.globalAssetId(), 1
            );
            case BANDAGE_KIT -> Map.of(
                    MedicalCraftingResource.LINEN.globalAssetId(), 2,
                    MedicalCraftingResource.HERBS.globalAssetId(), 1,
                    MedicalCraftingResource.HONEY.globalAssetId(), 1
            );
            case ANTIDOTE_KIT -> Map.of(
                    MedicalCraftingResource.ANTIDOTE_ROOT.globalAssetId(), 2,
                    MedicalCraftingResource.HERBS.globalAssetId(), 1,
                    MedicalCraftingResource.CLEAN_WATER.globalAssetId(), 1
            );
            case ARCANE_SALVE -> Map.of(
                    MedicalCraftingResource.CRYSTAL_MOSS.globalAssetId(), 2,
                    MedicalCraftingResource.HEALING_SAP.globalAssetId(), 1,
                    MedicalCraftingResource.CLEAN_WATER.globalAssetId(), 1
            );
        };
    }

    private Map<GlobalAssetId, Integer> multiply(Map<GlobalAssetId, Integer> baseRecipe, int quantity) {
        LinkedHashMap<GlobalAssetId, Integer> multiplied = new LinkedHashMap<>();
        for (Map.Entry<GlobalAssetId, Integer> entry : baseRecipe.entrySet()) {
            multiplied.put(entry.getKey(), entry.getValue() * quantity);
        }
        return Map.copyOf(multiplied);
    }
}
