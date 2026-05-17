package com.tavall.resourcegame.middleware.healing;

import com.tavall.resourcegame.middleware.common.GamePlatform;
import com.tavall.resourcegame.middleware.projection.PlatformInteractionType;
import com.tavall.resourcegame.middleware.troop.Troop;

import java.util.Optional;

public final class MinecraftTroopHealingProjectionHandler implements IHealingDomain {
    public MinecraftTroopHealingProjectionHandler() {
    }

    public MinecraftTroopHealingProjectionHandler(TroopHealingProjectionHandler troopHealingProjectionHandler) {
        registerTroopHealingProjectionHandler(troopHealingProjectionHandler);
    }

    public TroopHealingProjection projectTroopHealingForMinecraftClient(
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
                GamePlatform.MINECRAFT,
                PlatformInteractionType.MINECRAFT_COMMAND,
                PlatformInteractionType.MINECRAFT_COMMAND,
                PlatformInteractionType.MINECRAFT_COMMAND
        );
    }
}
