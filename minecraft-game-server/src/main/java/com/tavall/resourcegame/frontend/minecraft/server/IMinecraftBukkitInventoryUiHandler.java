package com.tavall.resourcegame.frontend.minecraft.server;

import com.tavall.resourcegame.api.internal.ui.UiPageType;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public interface IMinecraftBukkitInventoryUiHandler extends Listener, IDependencyInjectableInterface {
    void open(Player player, UiPageType pageType, String feedback);
}
