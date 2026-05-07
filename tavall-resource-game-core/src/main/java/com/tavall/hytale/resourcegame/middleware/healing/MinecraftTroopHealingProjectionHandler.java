package com.tavall.hytale.resourcegame.middleware.healing;

import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;
import com.tavall.hytale.resourcegame.middleware.projection.PlatformInteractionType;
import com.tavall.hytale.resourcegame.middleware.troop.Troop;

import java.util.Optional;

public final class MinecraftTroopHealingProjectionHandler {
    private final TroopHealingProjectionHandler troopHealingProjectionHandler;

    public MinecraftTroopHealingProjectionHandler(TroopHealingProjectionHandler troopHealingProjectionHandler) {
        this.troopHealingProjectionHandler = troopHealingProjectionHandler;
    }

    public TroopHealingProjection projectTroopHealingForMinecraftClient(
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
                GamePlatform.MINECRAFT,
                PlatformInteractionType.MINECRAFT_COMMAND,
                PlatformInteractionType.MINECRAFT_COMMAND,
                PlatformInteractionType.MINECRAFT_COMMAND
        );
    }
}
