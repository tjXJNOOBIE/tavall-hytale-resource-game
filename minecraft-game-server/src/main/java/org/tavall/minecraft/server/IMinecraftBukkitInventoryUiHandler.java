package org.tavall.minecraft.server;

import org.tavall.minecraft.framework.game.ui.UiScreenKey;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public interface IMinecraftBukkitInventoryUiHandler extends Listener, IDependencyInjectableInterface {
    void open(Player player, UiScreenKey pageType, String feedback);
}
