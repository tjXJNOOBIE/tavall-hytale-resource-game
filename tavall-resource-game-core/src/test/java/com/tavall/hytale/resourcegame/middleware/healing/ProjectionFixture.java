package com.tavall.hytale.resourcegame.middleware.healing;

import com.tavall.hytale.resourcegame.middleware.troop.Troop;

import java.util.Optional;

public record ProjectionFixture(
        TroopHealingProjectionHandler projectionHandler,
        Troop troop,
        TroopWound wound,
        HealingInventory rationOnlyInventory,
        Optional<HealingFacilityLevelDefinition> facility
) {
}
