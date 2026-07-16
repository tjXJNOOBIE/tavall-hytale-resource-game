package org.tavall.control.healing;

import org.tavall.control.troop.Troop;

import java.util.Optional;

public record ProjectionFixture(
        TroopHealingProjectionHandler projectionHandler,
        Troop troop,
        TroopWound wound,
        HealingInventory rationOnlyInventory,
        Optional<HealingFacilityLevelDefinition> facility
) {
}
