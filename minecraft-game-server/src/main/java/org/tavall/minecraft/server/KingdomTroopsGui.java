package org.tavall.minecraft.server;

import org.tavall.api.minecraft.ui.UiPageType;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.bukkit.entity.Player;

public final class KingdomTroopsGui implements IMinecraftBukkitServerDomain, IDependencyInjectableConcrete {
    public boolean open(Player player) {
        getKingdomInventoryUiHandler().open(player, UiPageType.CASTLE_TROOPS, "Troops.");
        return true;
    }
}
