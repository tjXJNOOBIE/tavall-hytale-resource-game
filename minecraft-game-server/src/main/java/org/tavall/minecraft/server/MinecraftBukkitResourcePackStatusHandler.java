package org.tavall.minecraft.server;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MinecraftBukkitResourcePackStatusHandler implements IMinecraftBukkitResourcePackStatusHandler, IBukkitUtilDependencyAccess, IDependencyInjectableConcrete {
    private static final long RESOURCE_PACK_STATUS_PROBE_DELAY_TICKS = 20L;
    private static final int RESOURCE_PACK_STATUS_PROBE_ATTEMPTS = 10;

    private final Map<UUID, PlayerResourcePackStatusEvent.Status> lastLoggedStatuses = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerResourcePackStatus(PlayerResourcePackStatusEvent event) {
        logResourcePackStatus(event.getPlayer(), event.getStatus(), "event");
    }

    public void probeResourcePackStatus(Player player) {
        probeResourcePackStatus(player, RESOURCE_PACK_STATUS_PROBE_ATTEMPTS);
    }

    private void probeResourcePackStatus(Player player, int attemptsRemaining) {
        if (player == null || !player.isOnline()) {
            return;
        }
        PlayerResourcePackStatusEvent.Status status = player.getResourcePackStatus();
        if (status == null) {
            if (attemptsRemaining <= 0) {
                getMinecraftBukkitLogger().info("Resource pack status from "
                        + player.getName()
                        + ": status=UNKNOWN url="
                        + getMinecraftBukkitResourcePackHandler().resourcePackUrl()
                        + " source=probe-timeout");
                return;
            }
            getMinecraftBukkitTaskScheduler().runLater(() -> probeResourcePackStatus(player, attemptsRemaining - 1), RESOURCE_PACK_STATUS_PROBE_DELAY_TICKS);
            return;
        }
        logResourcePackStatus(player, status, "probe");
    }

    private void logResourcePackStatus(Player player, PlayerResourcePackStatusEvent.Status status, String source) {
        UUID playerId = player.getUniqueId();
        PlayerResourcePackStatusEvent.Status previousStatus = lastLoggedStatuses.put(playerId, status);
        if (previousStatus == status) {
            return;
        }
        String resourcePackUrl = getMinecraftBukkitResourcePackHandler().resourcePackUrl();
        getMinecraftBukkitLogger().info("Resource pack status from "
                + player.getName()
                + ": status="
                + status
                + " url="
                + resourcePackUrl
                + " source="
                + source);
    }
}
