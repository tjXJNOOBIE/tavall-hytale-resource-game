package com.tavall.hytale.resourcegame.frontend.minecraft;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class ProxyServerSwitchGateway implements MinecraftVelocityInstanceSwitchGateway {
    private final ProxyServer proxyServer;
    private final Map<String, String> instanceServerMappings;

    public ProxyServerSwitchGateway(ProxyServer proxyServer, Map<String, String> instanceServerMappings) {
        this.proxyServer = proxyServer;
        this.instanceServerMappings = instanceServerMappings == null ? Map.of() : Map.copyOf(instanceServerMappings);
    }

    @Override
    public CompletableFuture<MinecraftVelocityInstanceSwitchResult> switchPlayer(String platformAccountId, String targetInstanceId) {
        UUID playerId;
        try {
            playerId = UUID.fromString(platformAccountId);
        } catch (RuntimeException exception) {
            return CompletableFuture.completedFuture(MinecraftVelocityInstanceSwitchResult.notAttempted("Velocity switch requires a player UUID account id."));
        }
        String targetServerName = instanceServerMappings.getOrDefault(targetInstanceId, targetInstanceId);
        Optional<RegisteredServer> targetServer = proxyServer.getServer(targetServerName);
        if (targetServer.isEmpty()) {
            return CompletableFuture.completedFuture(MinecraftVelocityInstanceSwitchResult.failed(targetServerName, "Velocity server is not registered: " + targetServerName + "."));
        }
        return proxyServer.getPlayer(playerId)
                .map(player -> player.createConnectionRequest(targetServer.get()).connect()
                        .thenApply(result -> result.isSuccessful()
                                ? MinecraftVelocityInstanceSwitchResult.success(targetServerName)
                                : MinecraftVelocityInstanceSwitchResult.failed(targetServerName, "Velocity connection request failed.")))
                .orElseGet(() -> CompletableFuture.completedFuture(MinecraftVelocityInstanceSwitchResult.notAttempted("Velocity player is not online.")));
    }
}
