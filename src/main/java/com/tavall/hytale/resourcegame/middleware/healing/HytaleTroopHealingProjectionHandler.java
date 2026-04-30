package com.tavall.hytale.resourcegame.middleware.healing;

import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;
import com.tavall.hytale.resourcegame.middleware.projection.PlatformInteractionType;
import com.tavall.hytale.resourcegame.middleware.troop.Troop;

import java.util.Optional;

public final class HytaleTroopHealingProjectionHandler {
    private final TroopHealingProjectionHandler troopHealingProjectionHandler;

    public HytaleTroopHealingProjectionHandler(TroopHealingProjectionHandler troopHealingProjectionHandler) {
        this.troopHealingProjectionHandler = troopHealingProjectionHandler;
    }

    public TroopHealingProjection projectTroopHealingForHytaleClient(
            Troop troop,
            TroopWound wound,
            HealingInventory inventory,
            Optional<HealingFacilityLevelDefinition> facility
    ) {
        return troopHealingProjectionHandler.projectTroopHealing(
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
