package org.tavall.minecraft.server;

import org.tavall.minecraft.framework.game.ui.UiScreenKey;
import org.tavall.dependency.IDependencyInjectableConcrete;
import org.bukkit.entity.Player;

public final class KingdomBuildingsGui implements MinecraftBukkitServerDomain, IDependencyInjectableConcrete {
    public boolean open(Player player) {
        getKingdomInventoryUiHandler().open(player, UiScreenKey.CASTLE_BUILDINGS, "Buildings.");
        return true;
    }
}
