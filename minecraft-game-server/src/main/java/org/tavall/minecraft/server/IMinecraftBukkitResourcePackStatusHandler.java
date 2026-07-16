package org.tavall.minecraft.server;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public interface IMinecraftBukkitResourcePackStatusHandler extends Listener, IDependencyInjectableInterface {
    void probeResourcePackStatus(Player player);
}
