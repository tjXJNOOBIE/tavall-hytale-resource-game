package org.tavall.minecraft.server;

import org.tavall.minecraft.framework.game.ui.UiScreenKey;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.bukkit.entity.Player;

public final class KingdomInteriorGui implements MinecraftBukkitServerDomain, IDependencyInjectableConcrete {
    public boolean open(Player player) {
        getKingdomInventoryUiHandler().open(player, UiScreenKey.INTERIOR_MAIN, "Interior.");
        return true;
    }
}
