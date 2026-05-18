package org.tavall.control.healing;

import org.tavall.control.event.DomainEventPublisher;
import org.tavall.control.event.SimpleDomainEvent;
import org.tavall.control.identity.UniversalPlayerId;
import org.tavall.control.troop.Troop;
import org.tavall.control.troop.TroopRepository;

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
