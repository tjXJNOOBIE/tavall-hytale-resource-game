package org.tavall.control.healing;

import org.tavall.control.asset.GlobalAssetId;
import org.tavall.control.event.RecordingDomainEventPublisher;
import org.tavall.control.identity.UniversalPlayerId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class MedicalItemCraftingHandlerIntegrationTest {
    @Test
    void fourHealingItemsCraftFromOlderEraNaturalResources() {
        InMemoryHealingInventoryRepository inventoryRepository = new InMemoryHealingInventoryRepository();
        RecordingDomainEventPublisher events = new RecordingDomainEventPublisher();
        MedicalItemCraftingHandler craftingHandler = new MedicalItemCraftingHandler(inventoryRepository, events);
        UniversalPlayerId playerId = UniversalPlayerId.random();
        HealingInventory inventory = new HealingInventory(Map.of())
                .withAdded(MedicalCraftingResource.FOOD_GRAIN.globalAssetId(), 10)
                .withAdded(MedicalCraftingResource.CLEAN_WATER.globalAssetId(), 10)
                .withAdded(MedicalCraftingResource.LINEN.globalAssetId(), 10)
                .withAdded(MedicalCraftingResource.HERBS.globalAssetId(), 10)
                .withAdded(MedicalCraftingResource.HONEY.globalAssetId(), 10)
                .withAdded(MedicalCraftingResource.ANTIDOTE_ROOT.globalAssetId(), 10)
                .withAdded(MedicalCraftingResource.CRYSTAL_MOSS.globalAssetId(), 10)
                .withAdded(MedicalCraftingResource.HEALING_SAP.globalAssetId(), 10);
        inventoryRepository.saveInventory(playerId, inventory);

        craftingHandler.craftHealingItem(playerId, HealingItemType.FIELD_RATIONS, 1, Instant.parse("2026-04-30T14:10:00Z"));
        craftingHandler.craftHealingItem(playerId, HealingItemType.BANDAGE_KIT, 1, Instant.parse("2026-04-30T14:11:00Z"));
        craftingHandler.craftHealingItem(playerId, HealingItemType.ANTIDOTE_KIT, 1, Instant.parse("2026-04-30T14:12:00Z"));
        HealingInventory updated = craftingHandler.craftHealingItem(playerId, HealingItemType.ARCANE_SALVE, 1, Instant.parse("2026-04-30T14:13:00Z"));

        assertEquals(1, updated.amount(HealingItemType.FIELD_RATIONS.globalAssetId()));
        assertEquals(1, updated.amount(HealingItemType.BANDAGE_KIT.globalAssetId()));
        assertEquals(1, updated.amount(HealingItemType.ANTIDOTE_KIT.globalAssetId()));
        assertEquals(1, updated.amount(HealingItemType.ARCANE_SALVE.globalAssetId()));
        assertEquals(4, events.publishedEvents().size());
    }

    @Test
    void missingCraftingResourceRejectsCrafting() {
        InMemoryHealingInventoryRepository inventoryRepository = new InMemoryHealingInventoryRepository();
        MedicalItemCraftingHandler craftingHandler = new MedicalItemCraftingHandler(inventoryRepository, new RecordingDomainEventPublisher());
        UniversalPlayerId playerId = UniversalPlayerId.random();
        inventoryRepository.saveInventory(playerId, new HealingInventory(Map.of(new GlobalAssetId("resource.food.grain"), 1)));

        assertThrows(HealingValidationException.class, () -> craftingHandler.craftHealingItem(playerId, HealingItemType.FIELD_RATIONS, 1, Instant.parse("2026-04-30T14:14:00Z")));
    }
}
