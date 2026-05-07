package com.tavall.hytale.resourcegame.frontend.minecraft.server;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MinecraftBukkitSnapshotHandler {
    private final MinecraftBukkitServerConfig config;
    private final long startedAtEpochMillis;

    public MinecraftBukkitSnapshotHandler(MinecraftBukkitServerConfig config, long startedAtEpochMillis) {
        this.config = config;
        this.startedAtEpochMillis = startedAtEpochMillis;
    }

    public MinecraftBukkitServerSnapshot createSnapshot(MinecraftBukkitServerView serverView, long observedAtEpochMillis) {
        List<MinecraftBukkitPlayerSnapshot> players = new ArrayList<MinecraftBukkitPlayerSnapshot>();
        for (MinecraftBukkitPlayerView playerView : serverView.onlinePlayers()) {
            players.add(new MinecraftBukkitPlayerSnapshot(playerView));
        }

        Map<String, String> metadata = new LinkedHashMap<String, String>();
        metadata.put("runtime", "spigot-bukkit");
        metadata.put("dataPath", "direct-control-ingress");
        metadata.put("visualSurface", "server-plugin");

        return new MinecraftBukkitServerSnapshot(
                "BUKKIT_SERVER",
                config.serverId(),
                config.proxyId(),
                serverView.hostname(),
                startedAtEpochMillis,
                observedAtEpochMillis,
                players.size(),
                serverView.maxPlayers(),
                players,
                metadata
        );
    }
}
