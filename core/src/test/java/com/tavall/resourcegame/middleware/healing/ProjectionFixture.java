package com.tavall.resourcegame.middleware.healing;

import com.tavall.resourcegame.middleware.troop.Troop;

import java.util.Optional;

public record ProjectionFixture(
        TroopHealingProjectionHandler projectionHandler,
        Troop troop,
        TroopWound wound,
        HealingInventory rationOnlyInventory,
        Optional<HealingFacilityLevelDefinition> facility
) {
}
