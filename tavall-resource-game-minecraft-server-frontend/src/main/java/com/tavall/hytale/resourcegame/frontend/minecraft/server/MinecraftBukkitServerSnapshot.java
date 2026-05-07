package com.tavall.hytale.resourcegame.frontend.minecraft.server;

import java.util.List;
import java.util.Map;

public final class MinecraftBukkitServerSnapshot {
    private final String surfaceIdentity;
    private final String serverId;
    private final String proxyId;
    private final String hostname;
    private final long startedAtEpochMillis;
    private final long observedAtEpochMillis;
    private final int onlinePlayerCount;
    private final int maxPlayerCount;
    private final List<MinecraftBukkitPlayerSnapshot> players;
    private final Map<String, String> metadata;

    public MinecraftBukkitServerSnapshot(
            String surfaceIdentity,
            String serverId,
            String proxyId,
            String hostname,
            long startedAtEpochMillis,
            long observedAtEpochMillis,
            int onlinePlayerCount,
            int maxPlayerCount,
            List<MinecraftBukkitPlayerSnapshot> players,
            Map<String, String> metadata
    ) {
        this.surfaceIdentity = surfaceIdentity;
        this.serverId = serverId;
        this.proxyId = proxyId;
        this.hostname = hostname;
        this.startedAtEpochMillis = startedAtEpochMillis;
        this.observedAtEpochMillis = observedAtEpochMillis;
        this.onlinePlayerCount = onlinePlayerCount;
        this.maxPlayerCount = maxPlayerCount;
        this.players = players;
        this.metadata = metadata;
    }

    public String getSurfaceIdentity() {
        return surfaceIdentity;
    }

    public String getServerId() {
        return serverId;
    }

    public String getProxyId() {
        return proxyId;
    }

    public String getHostname() {
        return hostname;
    }

    public long getStartedAtEpochMillis() {
        return startedAtEpochMillis;
    }

    public long getObservedAtEpochMillis() {
        return observedAtEpochMillis;
    }

    public int getOnlinePlayerCount() {
        return onlinePlayerCount;
    }

    public int getMaxPlayerCount() {
        return maxPlayerCount;
    }

    public List<MinecraftBukkitPlayerSnapshot> getPlayers() {
        return players;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }
}
