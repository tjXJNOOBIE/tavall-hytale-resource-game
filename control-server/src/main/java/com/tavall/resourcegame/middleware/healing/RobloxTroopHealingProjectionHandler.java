package org.tavall.control.healing;

import org.tavall.control.common.GamePlatform;
import org.tavall.control.projection.PlatformInteractionType;
import org.tavall.control.troop.Troop;

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
