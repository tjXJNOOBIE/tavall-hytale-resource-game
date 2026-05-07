package com.tavall.hytale.resourcegame.middleware.healing;

import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;
import com.tavall.hytale.resourcegame.middleware.projection.PlatformInteractionType;
import com.tavall.hytale.resourcegame.middleware.troop.Troop;

import java.util.Optional;

public final class RobloxTroopHealingProjectionHandler {
    private final TroopHealingProjectionHandler troopHealingProjectionHandler;

    public RobloxTroopHealingProjectionHandler(TroopHealingProjectionHandler troopHealingProjectionHandler) {
        this.troopHealingProjectionHandler = troopHealingProjectionHandler;
    }

    public TroopHealingProjection projectTroopHealingForRobloxClient(
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
                GamePlatform.ROBLOX,
                PlatformInteractionType.ROBLOX_REMOTE_EVENT,
                PlatformInteractionType.ROBLOX_GUI_ACTION,
                PlatformInteractionType.ROBLOX_GUI_ACTION
        );
    }
}
