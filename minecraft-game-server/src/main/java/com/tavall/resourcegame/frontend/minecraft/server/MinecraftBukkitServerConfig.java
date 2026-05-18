package org.tavall.minecraft.server;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.util.Map;

public final class MinecraftBukkitServerConfig implements IMinecraftBukkitServerConfig, IDependencyInjectableConcrete {
    private final String serverId;
    private final String proxyId;
    private final long snapshotIntervalTicks;
    private final String resourcePackPath;

    public MinecraftBukkitServerConfig(
            String serverId,
            String proxyId,
            long snapshotIntervalTicks,
            String resourcePackPath
    ) {
        this.serverId = serverId;
        this.proxyId = proxyId;
        this.snapshotIntervalTicks = snapshotIntervalTicks;
        this.resourcePackPath = resourcePackPath;
    }

    public static MinecraftBukkitServerConfig fromEnvironment(Map<String, String> environment) {
        return new MinecraftBukkitServerConfig(
                firstNonBlank(environment.get("RESOURCE_GAME_MINECRAFT_SERVER_ID"), "minecraft-bukkit-server"),
                firstNonBlank(environment.get("RESOURCE_GAME_MINECRAFT_PROXY_ID"), "velocity-proxy"),
                parseLong(firstNonBlank(environment.get("RESOURCE_GAME_MINECRAFT_SNAPSHOT_INTERVAL_TICKS"), "200"), 200L),
                firstNonBlank(environment.get("RESOURCE_GAME_MINECRAFT_RESOURCE_PACK_PATH"), "resource-pack/")
        );
    }

    @Override
    public String serverId() {
        return serverId;
    }

    @Override
    public String proxyId() {
        return proxyId;
    }

    @Override
    public long snapshotIntervalTicks() {
        return snapshotIntervalTicks;
    }

    @Override
    public String resourcePackPath() {
        return resourcePackPath;
    }

    private static String firstNonBlank(String candidate, String fallback) {
        return candidate == null || candidate.trim().isEmpty() ? fallback : candidate;
    }

    private static long parseLong(String value, long fallback) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }
}
