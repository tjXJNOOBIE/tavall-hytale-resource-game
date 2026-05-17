package com.tavall.resourcegame.middleware.healing;

import com.tavall.resourcegame.middleware.common.GamePlatform;
import com.tavall.resourcegame.middleware.projection.PlatformInteractionType;
import com.tavall.resourcegame.middleware.troop.Troop;

import java.util.Optional;

public final class RobloxTroopHealingProjectionHandler implements IHealingDomain {
    public RobloxTroopHealingProjectionHandler() {
    }

    public RobloxTroopHealingProjectionHandler(TroopHealingProjectionHandler troopHealingProjectionHandler) {
        registerTroopHealingProjectionHandler(troopHealingProjectionHandler);
    }

    public TroopHealingProjection projectTroopHealingForRobloxClient(
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
                GamePlatform.ROBLOX,
                PlatformInteractionType.ROBLOX_REMOTE_EVENT,
                PlatformInteractionType.ROBLOX_GUI_ACTION,
                PlatformInteractionType.ROBLOX_GUI_ACTION
        );
    }
}
