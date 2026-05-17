package com.tavall.resourcegame.frontend.minecraft.server;

import com.tavall.resourcegame.ui.UiPageType;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.bukkit.entity.Player;

public final class KingdomInteriorGui implements IMinecraftBukkitServerDomain, IDependencyInjectableConcrete {
    public boolean open(Player player) {
        getKingdomInventoryUiHandler().open(player, UiPageType.INTERIOR_MAIN, "Interior.");
        return true;
    }
}
