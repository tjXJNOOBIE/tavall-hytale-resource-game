package org.tavall.control.healing;

import org.tavall.control.asset.GlobalAssetId;
import org.tavall.control.asset.GlobalAssetResolutionHandler;
import org.tavall.control.asset.ResolvedPlatformAsset;
import org.tavall.control.common.GamePlatform;
import org.tavall.control.projection.InteractionAction;
import org.tavall.control.projection.PlatformInteractionType;
import org.tavall.control.troop.Troop;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class TroopHealingProjectionHandler implements IHealingDomain {
    public TroopHealingProjectionHandler() {
    }

    public TroopHealingProjectionHandler(
            TroopHealingRecipeSelectionHandler recipeSelectionHandler,
            TroopHealingRecipeValidationHandler recipeValidationHandler,
            GlobalAssetResolutionHandler globalAssetResolutionHandler
    ) {
        registerTroopHealingRecipeSelectionHandler(recipeSelectionHandler);
        registerTroopHealingRecipeValidationHandler(recipeValidationHandler);
        registerGlobalAssetResolutionHandler(globalAssetResolutionHandler);
    }

    public TroopHealingProjection projectTroopHealing(
            Troop troop,
            TroopWound wound,
            HealingInventory inventory,
            Optional<HealingFacilityLevelDefinition> facility,
            GamePlatform platform,
            PlatformInteractionType startInteractionType,
            PlatformInteractionType craftInteractionType,
            PlatformInteractionType viewInteractionType
    ) {
        ArrayList<TroopHealingProjectionOption> options = new ArrayList<>();
        ArrayList<InteractionAction> actions = new ArrayList<>();
        LinkedHashSet<GlobalAssetId> allAssetIds = new LinkedHashSet<>();
        allAssetIds.add(troop.globalAssetId());
        facility.map(HealingFacilityLevelDefinition::globalAssetId).ifPresent(allAssetIds::add);

        for (TroopHealingRecipe recipe : getTroopHealingRecipeSelectionHandler().recipesForWound(wound.woundType())) {
            HealingValidationResult validationResult = getTroopHealingRecipeValidationHandler().validateHealingRecipe(troop, wound, recipe, inventory, facility);
            LinkedHashSet<GlobalAssetId> optionAssetIds = new LinkedHashSet<>();
            optionAssetIds.addAll(validationResult.requiredResources().keySet());
            validationResult.requiredGemType().map(GemType::globalAssetId).ifPresent(optionAssetIds::add);
            facility.map(HealingFacilityLevelDefinition::globalAssetId).ifPresent(optionAssetIds::add);
            allAssetIds.addAll(optionAssetIds);
            String actionId = recipe.healingMode() == HealingMode.FOOD_ONLY
                    ? "action.healing.start_food_only"
                    : "action.healing.start_proper_treatment";
            TroopHealingProjectionOption option = new TroopHealingProjectionOption(
                    actionId,
                    recipe.recipeId(),
                    recipe.healingMode(),
                    validationResult.valid(),
                    validationResult.disabledReason(),
                    validationResult.requiredResources(),
                    validationResult.missingResources(),
                    validationResult.missingFacilityRequirement(),
                    validationResult.requiredGemType(),
                    validationResult.estimatedHealingTime(),
                    List.copyOf(optionAssetIds),
                    resolveAssets(optionAssetIds, platform),
                    startInteractionType
            );
            options.add(option);
            actions.add(new InteractionAction(
                    actionId,
                    recipe.healingMode() == HealingMode.FOOD_ONLY ? "Start food-only healing" : "Start proper treatment",
                    Optional.empty(),
                    Set.of(),
                    validationResult.valid(),
                    validationResult.disabledReason(),
                    startInteractionType,
                    Map.of("recipeId", recipe.recipeId(), "woundType", wound.woundType().name())
            ));
        }

        actions.add(new InteractionAction(
                "action.healing.craft_bandage_kit",
                "Craft Bandage Kit",
                Optional.empty(),
                Set.of(),
                true,
                Optional.empty(),
                craftInteractionType,
                Map.of("globalAssetId", HealingItemType.BANDAGE_KIT.globalAssetId().value())
        ));
        actions.add(new InteractionAction(
                "action.healing.craft_antidote_kit",
                "Craft Antidote Kit",
                Optional.empty(),
                Set.of(),
                true,
                Optional.empty(),
                craftInteractionType,
                Map.of("globalAssetId", HealingItemType.ANTIDOTE_KIT.globalAssetId().value())
        ));
        actions.add(new InteractionAction(
                "action.healing.craft_arcane_salve",
                "Craft Arcane Salve",
                Optional.empty(),
                Set.of(),
                true,
                Optional.empty(),
                craftInteractionType,
                Map.of("globalAssetId", HealingItemType.ARCANE_SALVE.globalAssetId().value())
        ));
        actions.add(new InteractionAction(
                "action.healing.view_missing_resources",
                "View missing resources",
                Optional.empty(),
                Set.of(),
                options.stream().anyMatch(option -> !option.missingResources().isEmpty()),
                options.stream().anyMatch(option -> !option.missingResources().isEmpty()) ? Optional.empty() : Optional.of("No missing resources."),
                viewInteractionType,
                Map.of("troopId", troop.troopId().value().toString())
        ));

        return new TroopHealingProjection(
                platform,
                troop.troopId(),
                wound.woundId(),
                wound.woundType(),
                wound.severity(),
                options,
                List.copyOf(allAssetIds),
                resolveAssets(allAssetIds, platform),
                actions,
                Map.of(
                        "canonicalStateOwner", "middleware-control-server",
                        "foodOnlyFallback", "Food-only recovery remains available when field rations exist, even when proper treatment is blocked."
                )
        );
    }

    private Map<GlobalAssetId, String> resolveAssets(Iterable<GlobalAssetId> globalAssetIds, GamePlatform platform) {
        LinkedHashMap<GlobalAssetId, String> resolvedAssets = new LinkedHashMap<>();
        for (GlobalAssetId globalAssetId : globalAssetIds) {
            ResolvedPlatformAsset resolved = getGlobalAssetResolutionHandler().resolveGlobalAssetForPlatform(globalAssetId, platform);
            resolvedAssets.put(globalAssetId, resolved.platformAssetReference().orElse(resolved.fallbackAssetKey()));
        }
        return Map.copyOf(resolvedAssets);
    }
}
