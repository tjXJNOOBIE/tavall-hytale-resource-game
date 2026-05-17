package com.tavall.resourcegame.middleware.healing;

import com.tavall.resourcegame.middleware.common.GamePlatform;
import com.tavall.resourcegame.middleware.projection.PlatformInteractionType;
import com.tavall.resourcegame.middleware.troop.Troop;

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
