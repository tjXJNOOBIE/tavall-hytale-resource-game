package com.tavall.resourcegame.frontend.minecraft.server.snapshot;

import com.tavall.resourcegame.api.internal.minecraft.MinecraftPlayerRuntimeSnapshot;
import com.tavall.resourcegame.api.internal.minecraft.MinecraftServerRuntimeSnapshot;
import com.tavall.resourcegame.api.internal.frontend.ResourceGameFrontendSurfaceIdentity;
import com.tavall.resourcegame.frontend.minecraft.server.IMinecraftBukkitServerDomain;
import com.tavall.resourcegame.frontend.minecraft.server.view.MinecraftBukkitPlayerView;
import com.tavall.resourcegame.frontend.minecraft.server.view.MinecraftBukkitServerView;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MinecraftBukkitSnapshotHandler implements IMinecraftBukkitSnapshotHandler, IMinecraftBukkitServerDomain, IDependencyInjectableConcrete {
    @Override
    public MinecraftServerRuntimeSnapshot createSnapshot(MinecraftBukkitServerView serverView, long observedAtEpochMillis) {
        List<MinecraftPlayerRuntimeSnapshot> players = new ArrayList<MinecraftPlayerRuntimeSnapshot>();
        for (MinecraftBukkitPlayerView playerView : serverView.onlinePlayers()) {
            players.add(new MinecraftPlayerRuntimeSnapshot(
                    playerView.playerId(),
                    playerView.playerName(),
                    playerView.worldName(),
                    playerView.x(),
                    playerView.y(),
                    playerView.z(),
                    playerView.yaw(),
                    playerView.pitch(),
                    playerView.health(),
                    playerView.foodLevel(),
                    playerView.gameMode(),
                    playerView.online(),
                    Map.of("source", "bukkit-server-plugin")
            ));
        }

        Map<String, String> metadata = new LinkedHashMap<String, String>();
        metadata.put("runtime", "spigot-bukkit");
        metadata.put("dataPath", "direct-control-ingress");
        metadata.put("visualSurface", "server-plugin");

        return new MinecraftServerRuntimeSnapshot(
                ResourceGameFrontendSurfaceIdentity.BUKKIT_SERVER,
                getMinecraftBukkitServerConfig().serverId(),
                getMinecraftBukkitServerConfig().proxyId(),
                serverView.hostname(),
                getMinecraftBukkitRuntimeState().startedAtEpochMillis(),
                observedAtEpochMillis,
                players.size(),
                serverView.maxPlayers(),
                players,
                metadata
        );
    }
}
