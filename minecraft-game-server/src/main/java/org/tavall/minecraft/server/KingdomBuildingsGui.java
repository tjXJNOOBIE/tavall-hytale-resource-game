package org.tavall.minecraft.server;

import org.tavall.api.minecraft.ui.UiPageType;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.bukkit.entity.Player;

public final class KingdomBuildingsGui implements IMinecraftBukkitServerDomain, IDependencyInjectableConcrete {
    public boolean open(Player player) {
        getKingdomInventoryUiHandler().open(player, UiPageType.CASTLE_BUILDINGS, "Buildings.");
        return true;
    }
}
