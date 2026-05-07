package com.tavall.hytale.resourcegame.frontend.minecraft.server;

import java.net.URI;
import java.util.Map;

public final class MinecraftBukkitServerConfig {
    private final URI snapshotIngressUri;
    private final URI commandIngressUri;
    private final String serverId;
    private final String proxyId;
    private final long snapshotIntervalTicks;

    public MinecraftBukkitServerConfig(
            URI snapshotIngressUri,
            URI commandIngressUri,
            String serverId,
            String proxyId,
            long snapshotIntervalTicks
    ) {
        this.snapshotIngressUri = snapshotIngressUri;
        this.commandIngressUri = commandIngressUri;
        this.serverId = serverId;
        this.proxyId = proxyId;
        this.snapshotIntervalTicks = snapshotIntervalTicks;
    }

    public static MinecraftBukkitServerConfig fromEnvironment(Map<String, String> environment) {
        String commandIngress = firstNonBlank(
                environment.get("RESOURCE_GAME_MINECRAFT_CONTROL_INGRESS_URL"),
                environment.get("RESOURCE_GAME_CONTROL_INGRESS_URL"),
                "http://127.0.0.1:8080/api/frontend/commands"
        );
        String snapshotIngress = firstNonBlank(
                environment.get("RESOURCE_GAME_MINECRAFT_CONTROL_SNAPSHOT_URL"),
                replaceCommandsPath(commandIngress),
                "http://127.0.0.1:8080/api/frontend/minecraft/server-snapshots"
        );
        return new MinecraftBukkitServerConfig(
                URI.create(snapshotIngress),
                URI.create(commandIngress),
                firstNonBlank(environment.get("RESOURCE_GAME_MINECRAFT_SERVER_ID"), "minecraft-bukkit-server"),
                firstNonBlank(environment.get("RESOURCE_GAME_MINECRAFT_PROXY_ID"), "velocity-proxy"),
                parseLong(firstNonBlank(environment.get("RESOURCE_GAME_MINECRAFT_SNAPSHOT_INTERVAL_TICKS"), "200"), 200L)
        );
    }

    public URI snapshotIngressUri() {
        return snapshotIngressUri;
    }

    public URI commandIngressUri() {
        return commandIngressUri;
    }

    public String serverId() {
        return serverId;
    }

    public String proxyId() {
        return proxyId;
    }

    public long snapshotIntervalTicks() {
        return snapshotIntervalTicks;
    }

    private static String replaceCommandsPath(String commandIngress) {
        if (commandIngress == null || commandIngress.trim().isEmpty()) {
            return "";
        }
        return commandIngress.replace("/api/frontend/commands", "/api/frontend/minecraft/server-snapshots");
    }

    private static String firstNonBlank(String candidate, String fallback) {
        return candidate == null || candidate.trim().isEmpty() ? fallback : candidate;
    }

    private static String firstNonBlank(String firstCandidate, String secondCandidate, String fallback) {
        String firstResolved = firstNonBlank(firstCandidate, "");
        return firstResolved.trim().isEmpty() ? firstNonBlank(secondCandidate, fallback) : firstResolved;
    }

    private static long parseLong(String value, long fallback) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }
}
