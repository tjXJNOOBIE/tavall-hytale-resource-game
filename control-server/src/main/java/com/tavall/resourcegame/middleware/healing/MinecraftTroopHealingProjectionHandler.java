package org.tavall.control.healing;

import org.tavall.control.common.GamePlatform;
import org.tavall.control.projection.PlatformInteractionType;
import org.tavall.control.troop.Troop;

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
