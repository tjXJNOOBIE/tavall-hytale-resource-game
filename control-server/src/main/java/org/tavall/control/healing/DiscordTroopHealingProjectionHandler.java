package org.tavall.control.healing;

import org.tavall.control.common.GamePlatform;
import org.tavall.control.projection.PlatformInteractionType;
import org.tavall.control.troop.Troop;

import java.util.Optional;

public final class DiscordTroopHealingProjectionHandler implements IHealingDomain {
    public DiscordTroopHealingProjectionHandler() {
    }

    public DiscordTroopHealingProjectionHandler(TroopHealingProjectionHandler troopHealingProjectionHandler) {
        registerTroopHealingProjectionHandler(troopHealingProjectionHandler);
    }

    public TroopHealingProjection projectTroopHealingForDiscordClient(
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
                GamePlatform.DISCORD,
                PlatformInteractionType.DISCORD_BUTTON,
                PlatformInteractionType.DISCORD_SELECT_MENU,
                PlatformInteractionType.DISCORD_SLASH_COMMAND
        );
    }
}
