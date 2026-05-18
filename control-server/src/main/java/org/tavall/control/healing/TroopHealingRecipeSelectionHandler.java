package org.tavall.control.healing;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class TroopHealingRecipeSelectionHandler {
    private final List<TroopHealingRecipe> recipes;

    public TroopHealingRecipeSelectionHandler() {
        this.recipes = List.of(
                foodOnly(WoundType.GENERAL_WOUND, 3, Duration.ofHours(8L)),
                foodOnly(WoundType.POISONED, 5, Duration.ofHours(24L)),
                foodOnly(WoundType.MAGIC_WOUND, 6, Duration.ofHours(36L)),
                foodOnly(WoundType.EXHAUSTED, 2, Duration.ofHours(4L)),
                proper(WoundType.GENERAL_WOUND, HealingItemType.BANDAGE_KIT, Set.of(GemType.PEARL), 1, 1, Duration.ofHours(2L)),
                proper(WoundType.POISONED, HealingItemType.ANTIDOTE_KIT, Set.of(GemType.AMETHYST), 12, 2, Duration.ofHours(4L)),
                proper(WoundType.MAGIC_WOUND, HealingItemType.ARCANE_SALVE, Set.of(GemType.AMETHYST), 24, 3, Duration.ofHours(6L)),
                exhaustedProper()
        );
    }

    public List<TroopHealingRecipe> recipesForWound(WoundType woundType) {
        return recipes.stream()
                .filter(recipe -> recipe.woundType() == woundType)
                .toList();
    }

    public Optional<TroopHealingRecipe> findRecipe(String recipeId) {
        return recipes.stream()
                .filter(recipe -> recipe.recipeId().equals(recipeId))
                .findFirst();
    }

    public TroopHealingRecipe foodOnlyRecipeFor(WoundType woundType) {
        return recipesForWound(woundType).stream()
                .filter(recipe -> recipe.healingMode() == HealingMode.FOOD_ONLY)
                .findFirst()
                .orElseThrow(() -> new HealingValidationException("Food-only recipe missing for " + woundType + "."));
    }

    public TroopHealingRecipe properTreatmentRecipeFor(WoundType woundType) {
        return recipesForWound(woundType).stream()
                .filter(recipe -> recipe.healingMode() == HealingMode.PROPER_TREATMENT)
                .findFirst()
                .orElseThrow(() -> new HealingValidationException("Proper treatment recipe missing for " + woundType + "."));
    }

    private TroopHealingRecipe foodOnly(WoundType woundType, int rationCount, Duration duration) {
        return new TroopHealingRecipe(
                "recipe.healing." + woundType.name().toLowerCase() + ".food_only",
                woundType,
                HealingMode.FOOD_ONLY,
                List.of(new HealingResourceStack(HealingItemType.FIELD_RATIONS.globalAssetId(), rationCount)),
                List.of(),
                Set.of(),
                0,
                duration,
                0.08d,
                1.0d,
                Map.of("projectionReason", "Food-only recovery is always available when rations exist, but it takes longer than proper treatment.")
        );
    }

    private TroopHealingRecipe proper(WoundType woundType, HealingItemType healingItem, Set<GemType> gemTypes, int facilityLevel, int rationCount, Duration duration) {
        return new TroopHealingRecipe(
                "recipe.healing." + woundType.name().toLowerCase() + ".proper",
                woundType,
                HealingMode.PROPER_TREATMENT,
                List.of(new HealingResourceStack(HealingItemType.FIELD_RATIONS.globalAssetId(), rationCount)),
                List.of(new HealingResourceStack(healingItem.globalAssetId(), 1)),
                gemTypes,
                facilityLevel,
                duration,
                0.1d,
                1.0d,
                Map.of()
        );
    }

    private TroopHealingRecipe exhaustedProper() {
        return new TroopHealingRecipe(
                "recipe.healing.exhausted.proper",
                WoundType.EXHAUSTED,
                HealingMode.PROPER_TREATMENT,
                List.of(new HealingResourceStack(HealingItemType.FIELD_RATIONS.globalAssetId(), 2)),
                List.of(),
                Set.of(),
                16,
                Duration.ofHours(1L),
                0.08d,
                1.0d,
                Map.of("projectionReason", "Field Hospital or better recovery uses rations and staff rotation instead of a separate exposed consumable.")
        );
    }
}
