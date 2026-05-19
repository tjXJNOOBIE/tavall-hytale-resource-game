package org.tavall.minecraft.server;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Keeps player-join event work bounded to local visuals and async backend snapshot handoff.
 */
public final class MinecraftBukkitPlayerJoinHandler implements IMinecraftBukkitPlayerJoinHandler, IBukkitUtilDependencyAccess, IDependencyInjectableConcrete {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        getMinecraftBukkitResourcePackHandler().forceResourcePack(player);
        getMinecraftBukkitResourcePackStatusHandler().probeResourcePackStatus(player);
        getMinecraftBukkitVisualHandler().renderJoinVisual(player, getMinecraftBukkitServerConfig().serverId());
        getMinecraftBukkitTaskScheduler().runAsync(getMinecraftBukkitSnapshotSubmitHandler()::submitSnapshotQuietly);
    }
}
