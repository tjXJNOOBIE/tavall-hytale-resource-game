package org.tavall.minecraft.switching;

import org.tavall.minecraft.bridge.IMinecraftFrontendBridgeDependencyAccess;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class ProxyServerSwitchGateway implements MinecraftVelocityInstanceSwitchGateway, IMinecraftFrontendBridgeDependencyAccess, IMinecraftFrontendSwitchingDependencyAccess {
    @Override
    public CompletableFuture<MinecraftVelocityInstanceSwitchResult> switchPlayer(String platformAccountId, String targetInstanceId) {
        UUID playerId;
        try {
            playerId = UUID.fromString(platformAccountId);
        } catch (RuntimeException exception) {
            return CompletableFuture.completedFuture(MinecraftVelocityInstanceSwitchResult.notAttempted("Velocity switch requires a player UUID account id."));
        }
        String targetServerName = getMinecraftProxyConfig().instanceServerMappings().getOrDefault(targetInstanceId, targetInstanceId);
        Optional<RegisteredServer> targetServer = getMinecraftVelocityProxyServer().getServer(targetServerName);
        if (targetServer.isEmpty()) {
            return CompletableFuture.completedFuture(MinecraftVelocityInstanceSwitchResult.failed(targetServerName, "Velocity server is not registered: " + targetServerName + "."));
        }
        return getMinecraftVelocityProxyServer().getPlayer(playerId)
                .map(player -> player.createConnectionRequest(targetServer.get()).connect()
                        .thenApply(result -> result.isSuccessful()
                                ? MinecraftVelocityInstanceSwitchResult.success(targetServerName)
                                : MinecraftVelocityInstanceSwitchResult.failed(targetServerName, "Velocity connection request failed.")))
                .orElseGet(() -> CompletableFuture.completedFuture(MinecraftVelocityInstanceSwitchResult.notAttempted("Velocity player is not online.")));
    }
}
