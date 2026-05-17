package com.tavall.resourcegame.frontend.minecraft.server;

import com.tavall.resourcegame.api.internal.ui.UiPageType;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.bukkit.entity.Player;

public final class KingdomResourcesGui implements IMinecraftBukkitServerDomain, IDependencyInjectableConcrete {
    public boolean open(Player player) {
        getKingdomInventoryUiHandler().open(player, UiPageType.CASTLE_RESOURCES, "Resources.");
        return true;
    }
}
