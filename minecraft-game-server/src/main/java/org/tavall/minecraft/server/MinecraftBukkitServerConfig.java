package org.tavall.minecraft.server;

import org.tavall.dependency.IDependencyInjectableConcrete;

import java.util.Map;

public final class MinecraftBukkitServerConfig implements IMinecraftBukkitServerConfig, IDependencyInjectableConcrete {
    private static final String DEFAULT_RESOURCE_PACK_URL = "";
    private static final String DEFAULT_RESOURCE_PACK_PROMPT = "Tavall Resource Game requires the resource pack to render kingdom UI.";
    private static final boolean DEFAULT_RESOURCE_PACK_FORCE = true;
    private static final int DEFAULT_RESOURCE_PACK_FORMAT = 34;

    private final String serverId;
    private final String proxyId;
    private final long snapshotIntervalTicks;
    private final String resourcePackPath;
    private final String resourcePackUrl;
    private final String resourcePackPrompt;
    private final boolean resourcePackForce;
    private final int resourcePackFormat;

    public MinecraftBukkitServerConfig(
            String serverId,
            String proxyId,
            long snapshotIntervalTicks,
            String resourcePackPath
    ) {
        this(
                serverId,
                proxyId,
                snapshotIntervalTicks,
                resourcePackPath,
                DEFAULT_RESOURCE_PACK_URL,
                DEFAULT_RESOURCE_PACK_PROMPT,
                DEFAULT_RESOURCE_PACK_FORCE,
                DEFAULT_RESOURCE_PACK_FORMAT
        );
    }

    public MinecraftBukkitServerConfig(
            String serverId,
            String proxyId,
            long snapshotIntervalTicks,
            String resourcePackPath,
            String resourcePackUrl,
            String resourcePackPrompt,
            boolean resourcePackForce,
            int resourcePackFormat
    ) {
        this.serverId = serverId;
        this.proxyId = proxyId;
        this.snapshotIntervalTicks = snapshotIntervalTicks;
        this.resourcePackPath = resourcePackPath;
        this.resourcePackUrl = resourcePackUrl;
        this.resourcePackPrompt = resourcePackPrompt;
        this.resourcePackForce = resourcePackForce;
        this.resourcePackFormat = resourcePackFormat;
    }

    public static MinecraftBukkitServerConfig fromEnvironment(Map<String, String> environment) {
        return new MinecraftBukkitServerConfig(
                firstNonBlank(environment.get("RESOURCE_GAME_MINECRAFT_SERVER_ID"), "minecraft-bukkit-server"),
                firstNonBlank(environment.get("RESOURCE_GAME_MINECRAFT_PROXY_ID"), "velocity-proxy"),
                parseLong(firstNonBlank(environment.get("RESOURCE_GAME_MINECRAFT_SNAPSHOT_INTERVAL_TICKS"), "200"), 200L),
                firstNonBlank(environment.get("RESOURCE_GAME_MINECRAFT_RESOURCE_PACK_PATH"), "resource-pack/"),
                firstNonBlank(environment.get("RESOURCE_GAME_MINECRAFT_RESOURCE_PACK_URL"), DEFAULT_RESOURCE_PACK_URL),
                firstNonBlank(environment.get("RESOURCE_GAME_MINECRAFT_RESOURCE_PACK_PROMPT"), DEFAULT_RESOURCE_PACK_PROMPT),
                Boolean.parseBoolean(firstNonBlank(environment.get("RESOURCE_GAME_MINECRAFT_RESOURCE_PACK_FORCE"), String.valueOf(DEFAULT_RESOURCE_PACK_FORCE))),
                (int) parseLong(firstNonBlank(environment.get("RESOURCE_GAME_MINECRAFT_RESOURCE_PACK_FORMAT"), String.valueOf(DEFAULT_RESOURCE_PACK_FORMAT)), DEFAULT_RESOURCE_PACK_FORMAT)
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

    @Override
    public String resourcePackUrl() {
        return resourcePackUrl;
    }

    @Override
    public String resourcePackPrompt() {
        return resourcePackPrompt;
    }

    @Override
    public boolean resourcePackForce() {
        return resourcePackForce;
    }

    @Override
    public int resourcePackFormat() {
        return resourcePackFormat;
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
