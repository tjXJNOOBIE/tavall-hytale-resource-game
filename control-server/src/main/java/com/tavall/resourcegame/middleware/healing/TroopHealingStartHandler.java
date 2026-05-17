package com.tavall.resourcegame.middleware.healing;

import com.tavall.resourcegame.middleware.event.DomainEventPublisher;
import com.tavall.resourcegame.middleware.event.SimpleDomainEvent;
import com.tavall.resourcegame.middleware.identity.UniversalPlayerId;
import com.tavall.resourcegame.middleware.troop.Troop;
import com.tavall.resourcegame.middleware.troop.TroopRepository;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public final class TroopHealingStartHandler implements IHealingDomain {
    public TroopHealingStartHandler() {
    }

    public TroopHealingStartHandler(
            TroopRepository troopRepository,
            TroopHealingRepository troopHealingRepository,
            HealingInventoryRepository healingInventoryRepository,
            TroopHealingRecipeValidationHandler recipeValidationHandler,
            DomainEventPublisher domainEventPublisher
    ) {
        registerTroopRepository(troopRepository);
        registerTroopHealingRepository(troopHealingRepository);
        registerHealingInventoryRepository(healingInventoryRepository);
        registerTroopHealingRecipeValidationHandler(recipeValidationHandler);
        registerDomainEventPublisher(domainEventPublisher);
    }

    public TroopHealingPlan startHealing(
            UniversalPlayerId universalPlayerId,
            TroopWound wound,
            TroopHealingRecipe recipe,
            Optional<HealingFacilityLevelDefinition> facility,
            Instant now
    ) {
        Troop troop = getTroopRepository().findTroop(wound.troopId())
                .orElseThrow(() -> new HealingValidationException("Troop was not found."));
        if (getTroopHealingRepository().findActiveHealingPlanForTroop(troop.troopId()).isPresent()) {
            throw new HealingValidationException("Troop already has an active healing plan.");
        }
        HealingInventory inventory = getHealingInventoryRepository().findInventory(universalPlayerId)
                .orElse(new HealingInventory(Map.of()));
        HealingValidationResult validationResult = getTroopHealingRecipeValidationHandler().validateHealingRecipe(troop, wound, recipe, inventory, facility);
        if (!validationResult.valid()) {
            throw new HealingValidationException(validationResult.disabledReason().orElse("Healing recipe is not valid."));
        }
        getHealingInventoryRepository().saveInventory(universalPlayerId, inventory.withConsumed(validationResult.requiredResources()));
        TroopHealingPlan plan = TroopHealingPlan.active(
                troop.troopId(),
                wound,
                recipe,
                facility,
                now,
                now.plus(validationResult.estimatedHealingTime()),
                validationResult.requiredResources()
        );
        TroopHealingPlan savedPlan = getTroopHealingRepository().saveHealingPlan(plan);
        getDomainEventPublisher().publish(new SimpleDomainEvent(
                "TroopHealingStartedEvent",
                now,
                Map.of("troopId", troop.troopId().value().toString(), "healingPlanId", savedPlan.healingPlanId().toString(), "recipeId", recipe.recipeId())
        ));
        getDomainEventPublisher().publish(new SimpleDomainEvent(
                "HealingResourcesConsumedEvent",
                now,
                Map.of("troopId", troop.troopId().value().toString(), "healingPlanId", savedPlan.healingPlanId().toString())
        ));
        return savedPlan;
    }
}
