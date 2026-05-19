package org.tavall.minecraft.server;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public final class MinecraftBukkitResourcePackStatusHandler implements IMinecraftBukkitResourcePackStatusHandler, IBukkitUtilDependencyAccess, IDependencyInjectableConcrete {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerResourcePackStatus(PlayerResourcePackStatusEvent event) {
        String playerName = event.getPlayer().getName();
        String resourcePackUrl = getMinecraftBukkitResourcePackHandler().resourcePackUrl();
        getMinecraftBukkitLogger().info("Resource pack status from "
                + playerName
                + ": status="
                + event.getStatus()
                + " url="
                + resourcePackUrl);
    }
}
