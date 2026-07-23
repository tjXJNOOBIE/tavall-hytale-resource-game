package org.tavall.minecraft.runtime;

import org.tavall.dependency.IDependencyInjectableConcrete;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class MinecraftVelocityProxyServerAdapter implements IMinecraftVelocityProxyServer, IDependencyInjectableConcrete {
    private final ProxyServer proxyServer;

    public MinecraftVelocityProxyServerAdapter(ProxyServer proxyServer) {
        this.proxyServer = Objects.requireNonNull(proxyServer, "proxyServer");
    }

    @Override
    public Optional<RegisteredServer> getServer(String serverName) {
        return proxyServer.getServer(serverName);
    }

    @Override
    public Optional<Player> getPlayer(UUID playerId) {
        return proxyServer.getPlayer(playerId);
    }

    @Override
    public Optional<Player> getPlayer(String username) {
        return proxyServer.getPlayer(username);
    }
}
