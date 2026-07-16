package org.tavall.control.healing;

import org.tavall.control.common.CanonicalLocation;
import org.tavall.control.event.RecordingDomainEventPublisher;
import org.tavall.control.identity.UniversalPlayerId;
import org.tavall.control.troop.InMemoryTroopRepository;
import org.tavall.control.troop.Troop;
import org.tavall.control.troop.TroopRegistrationHandler;
import org.tavall.control.troop.TroopStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class TroopHealingFlowIntegrationTest {
    @Test
    void assignStartTickAndCompleteHealingThroughMiddlewareState() {
        InMemoryTroopRepository troopRepository = new InMemoryTroopRepository();
        InMemoryTroopHealingRepository healingRepository = new InMemoryTroopHealingRepository();
        InMemoryHealingInventoryRepository inventoryRepository = new InMemoryHealingInventoryRepository();
        RecordingDomainEventPublisher events = new RecordingDomainEventPublisher();
        UniversalPlayerId playerId = UniversalPlayerId.random();
        Troop troop = new TroopRegistrationHandler(troopRepository)
                .registerTroop(Optional.of(playerId), Optional.empty(), "infantry", 2, new CanonicalLocation("world", 0.0d, 64.0d, 0.0d));
        Instant now = Instant.parse("2026-04-30T14:50:00Z");
        TroopWound wound = new TroopWoundAssignmentHandler(troopRepository, healingRepository, events)
                .assignWound(troop.troopId(), WoundType.GENERAL_WOUND, WoundSeverity.MINOR, now);
        inventoryRepository.saveInventory(playerId, new HealingInventory(Map.of())
                .withAdded(HealingItemType.FIELD_RATIONS.globalAssetId(), 20)
                .withAdded(HealingItemType.BANDAGE_KIT.globalAssetId(), 20)
                .withAdded(GemType.PEARL.globalAssetId(), 5));
        HealingFacilityModifierCalculationHandler modifierCalculationHandler = new HealingFacilityModifierCalculationHandler();
        TroopHealingRecipeValidationHandler validationHandler = new TroopHealingRecipeValidationHandler(new HealingResourceCostCalculationHandler(modifierCalculationHandler), modifierCalculationHandler);
        TroopHealingStartHandler startHandler = new TroopHealingStartHandler(troopRepository, healingRepository, inventoryRepository, validationHandler, events);

        TroopHealingPlan plan = startHandler.startHealing(
                playerId,
                wound,
                new TroopHealingRecipeSelectionHandler().properTreatmentRecipeFor(WoundType.GENERAL_WOUND),
                new HealingFacilityDefinitionRegistry().definitionForLevel(4),
                now.plusSeconds(1)
        );
        TroopHealingCompletionHandler completionHandler = new TroopHealingCompletionHandler(troopRepository, healingRepository, events);
        TroopHealingProgressTickHandler tickHandler = new TroopHealingProgressTickHandler(healingRepository, completionHandler, events);

        assertEquals(HealingState.ACTIVE, plan.state());
        assertTrue(inventoryRepository.findInventory(playerId).orElseThrow().amount(HealingItemType.BANDAGE_KIT.globalAssetId()) < 20);
        TroopHealingPlan completed = tickHandler.runHealingProgressTick(plan.completesAt()).getFirst();

        assertEquals(HealingState.COMPLETED, completed.state());
        assertTrue(healingRepository.findWound(wound.woundId()).orElseThrow().healedAt().isPresent());
        assertEquals(TroopStatus.IDLE, troopRepository.findTroop(troop.troopId()).orElseThrow().status());
        assertTrue(events.publishedEvents().stream().anyMatch(event -> event.eventType().equals("TroopHealingCompletedEvent")));
    }
}
