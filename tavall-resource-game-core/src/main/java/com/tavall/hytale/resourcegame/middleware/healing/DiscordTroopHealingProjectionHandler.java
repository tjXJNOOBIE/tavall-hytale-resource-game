package com.tavall.hytale.resourcegame.middleware.healing;

import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;
import com.tavall.hytale.resourcegame.middleware.projection.PlatformInteractionType;
import com.tavall.hytale.resourcegame.middleware.troop.Troop;

import java.util.Optional;

public final class DiscordTroopHealingProjectionHandler {
    private final TroopHealingProjectionHandler troopHealingProjectionHandler;

    public DiscordTroopHealingProjectionHandler(TroopHealingProjectionHandler troopHealingProjectionHandler) {
        this.troopHealingProjectionHandler = troopHealingProjectionHandler;
    }

    public TroopHealingProjection projectTroopHealingForDiscordClient(
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
                GamePlatform.DISCORD,
                PlatformInteractionType.DISCORD_BUTTON,
                PlatformInteractionType.DISCORD_SELECT_MENU,
                PlatformInteractionType.DISCORD_SLASH_COMMAND
        );
    }
}
