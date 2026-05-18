package org.tavall.control.healing;

import org.tavall.control.common.GamePlatform;
import org.tavall.control.projection.PlatformInteractionType;
import org.tavall.control.troop.Troop;

import java.util.Optional;

public final class HytaleTroopHealingProjectionHandler implements IHealingDomain {
    public HytaleTroopHealingProjectionHandler() {
    }

    public HytaleTroopHealingProjectionHandler(TroopHealingProjectionHandler troopHealingProjectionHandler) {
        registerTroopHealingProjectionHandler(troopHealingProjectionHandler);
    }

    public TroopHealingProjection projectTroopHealingForHytaleClient(
            Troop troop,
            TroopWound wound,
            HealingInventory inventory,
            Optional<HealingFacilityLevelDefinition> facility
    ) {
        return getTroopHealingProjectionHandler().projectTroopHealing(
                troop,
                wound,
                inventory,
                facility,
                GamePlatform.HYTALE,
                PlatformInteractionType.HYTALE_UI_ACTION,
                PlatformInteractionType.HYTALE_UI_ACTION,
                PlatformInteractionType.HYTALE_BOT_TEST_ACTION
        );
    }
}
