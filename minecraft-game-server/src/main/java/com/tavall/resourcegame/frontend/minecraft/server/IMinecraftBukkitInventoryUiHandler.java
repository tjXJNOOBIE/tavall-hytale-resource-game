package org.tavall.minecraft.server;

import org.tavall.api.minecraft.ui.UiPageType;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public interface IMinecraftBukkitInventoryUiHandler extends Listener, IDependencyInjectableInterface {
    void open(Player player, UiPageType pageType, String feedback);
}
