package com.tavall.hytale.resourcegame.frontend.minecraft.server;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MinecraftBukkitServerFrontendTest {
    @Test
    void configDerivesSnapshotIngressFromCommandIngress() {
        Map<String, String> environment = new LinkedHashMap<String, String>();
        environment.put("RESOURCE_GAME_MINECRAFT_CONTROL_INGRESS_URL", "http://127.0.0.1:18080/api/frontend/commands");
        environment.put("RESOURCE_GAME_MINECRAFT_SERVER_ID", "ffa");
        environment.put("RESOURCE_GAME_MINECRAFT_PROXY_ID", "velocity-proxy");
        environment.put("RESOURCE_GAME_MINECRAFT_SNAPSHOT_INTERVAL_TICKS", "40");
        MinecraftBukkitServerConfig config = MinecraftBukkitServerConfig.fromEnvironment(environment);

        assertEquals(URI.create("http://127.0.0.1:18080/api/frontend/minecraft/server-snapshots"), config.snapshotIngressUri());
        assertEquals("ffa", config.serverId());
        assertEquals("velocity-proxy", config.proxyId());
        assertEquals(40L, config.snapshotIntervalTicks());
    }

    @Test
    void snapshotHandlerBuildsBukkitServerSurfacePayload() {
        MinecraftBukkitServerConfig config = new MinecraftBukkitServerConfig(
                URI.create("http://127.0.0.1:18080/api/frontend/minecraft/server-snapshots"),
                URI.create("http://127.0.0.1:18080/api/frontend/commands"),
                "ffa",
                "velocity-proxy",
                200L
        );
        MinecraftBukkitSnapshotHandler handler = new MinecraftBukkitSnapshotHandler(config, 1000L);

        MinecraftBukkitServerSnapshot snapshot = handler.createSnapshot(new FakeServerView(), 2000L);

        assertEquals("BUKKIT_SERVER", snapshot.getSurfaceIdentity());
        assertEquals("ffa", snapshot.getServerId());
        assertEquals("velocity-proxy", snapshot.getProxyId());
        assertEquals(1, snapshot.getOnlinePlayerCount());
        assertEquals("Miner", snapshot.getPlayers().get(0).getPlayerName());
        assertEquals("direct-control-ingress", snapshot.getMetadata().get("dataPath"));
    }

    @Test
    void jsonHandlerSerializesSnapshotShapeAcceptedByControlIngress() throws Exception {
        MinecraftBukkitServerConfig config = new MinecraftBukkitServerConfig(
                URI.create("http://127.0.0.1:18080/api/frontend/minecraft/server-snapshots"),
                URI.create("http://127.0.0.1:18080/api/frontend/commands"),
                "ffa",
                "velocity-proxy",
                200L
        );
        MinecraftBukkitServerSnapshot snapshot = new MinecraftBukkitSnapshotHandler(config, 1000L)
                .createSnapshot(new FakeServerView(), 2000L);

        String json = new MinecraftBukkitJsonHandler().writeJson(snapshot);

        assertTrue(json.contains("\"surfaceIdentity\":\"BUKKIT_SERVER\""));
        assertTrue(json.contains("\"serverId\":\"ffa\""));
        assertTrue(json.contains("\"playerName\":\"Miner\""));
    }

    private static final class FakeServerView implements MinecraftBukkitServerView {
        @Override
        public String hostname() {
            return "minecraft-host";
        }

        @Override
        public int maxPlayers() {
            return 100;
        }

        @Override
        public Collection<MinecraftBukkitPlayerView> onlinePlayers() {
            return Arrays.<MinecraftBukkitPlayerView>asList(new FakePlayerView());
        }
    }

    private static final class FakePlayerView implements MinecraftBukkitPlayerView {
        @Override
        public UUID playerId() {
            return UUID.fromString("00000000-0000-0000-0000-000000000001");
        }

        @Override
        public String playerName() {
            return "Miner";
        }

        @Override
        public String worldName() {
            return "world";
        }

        @Override
        public double x() {
            return 1.0d;
        }

        @Override
        public double y() {
            return 64.0d;
        }

        @Override
        public double z() {
            return 2.0d;
        }

        @Override
        public float yaw() {
            return 90.0f;
        }

        @Override
        public float pitch() {
            return 20.0f;
        }

        @Override
        public double health() {
            return 18.0d;
        }

        @Override
        public int foodLevel() {
            return 20;
        }

        @Override
        public String gameMode() {
            return "SURVIVAL";
        }

        @Override
        public boolean online() {
            return true;
        }
    }
}
