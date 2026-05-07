package com.tavall.hytale.resourcegame.middleware.healing;

import com.tavall.hytale.resourcegame.middleware.event.DomainEventPublisher;
import com.tavall.hytale.resourcegame.middleware.event.SimpleDomainEvent;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;
import com.tavall.hytale.resourcegame.middleware.troop.Troop;
import com.tavall.hytale.resourcegame.middleware.troop.TroopRepository;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public final class TroopHealingStartHandler {
    private final TroopRepository troopRepository;
    private final TroopHealingRepository troopHealingRepository;
    private final HealingInventoryRepository healingInventoryRepository;
    private final TroopHealingRecipeValidationHandler recipeValidationHandler;
    private final DomainEventPublisher domainEventPublisher;

    public TroopHealingStartHandler(
            TroopRepository troopRepository,
            TroopHealingRepository troopHealingRepository,
            HealingInventoryRepository healingInventoryRepository,
            TroopHealingRecipeValidationHandler recipeValidationHandler,
            DomainEventPublisher domainEventPublisher
    ) {
        this.troopRepository = troopRepository;
        this.troopHealingRepository = troopHealingRepository;
        this.healingInventoryRepository = healingInventoryRepository;
        this.recipeValidationHandler = recipeValidationHandler;
        this.domainEventPublisher = domainEventPublisher;
    }

    public TroopHealingPlan startHealing(
            UniversalPlayerId universalPlayerId,
            TroopWound wound,
            TroopHealingRecipe recipe,
            Optional<HealingFacilityLevelDefinition> facility,
            Instant now
    ) {
        Troop troop = troopRepository.findTroop(wound.troopId())
                .orElseThrow(() -> new HealingValidationException("Troop was not found."));
        if (troopHealingRepository.findActiveHealingPlanForTroop(troop.troopId()).isPresent()) {
            throw new HealingValidationException("Troop already has an active healing plan.");
        }
        HealingInventory inventory = healingInventoryRepository.findInventory(universalPlayerId)
                .orElse(new HealingInventory(Map.of()));
        HealingValidationResult validationResult = recipeValidationHandler.validateHealingRecipe(troop, wound, recipe, inventory, facility);
        if (!validationResult.valid()) {
            throw new HealingValidationException(validationResult.disabledReason().orElse("Healing recipe is not valid."));
        }
        healingInventoryRepository.saveInventory(universalPlayerId, inventory.withConsumed(validationResult.requiredResources()));
        TroopHealingPlan plan = TroopHealingPlan.active(
                troop.troopId(),
                wound,
                recipe,
                facility,
                now,
                now.plus(validationResult.estimatedHealingTime()),
                validationResult.requiredResources()
        );
        TroopHealingPlan savedPlan = troopHealingRepository.saveHealingPlan(plan);
        domainEventPublisher.publish(new SimpleDomainEvent(
                "TroopHealingStartedEvent",
                now,
                Map.of("troopId", troop.troopId().value().toString(), "healingPlanId", savedPlan.healingPlanId().toString(), "recipeId", recipe.recipeId())
        ));
        domainEventPublisher.publish(new SimpleDomainEvent(
                "HealingResourcesConsumedEvent",
                now,
                Map.of("troopId", troop.troopId().value().toString(), "healingPlanId", savedPlan.healingPlanId().toString())
        ));
        return savedPlan;
    }
}
