package org.tavall.minecraft.runtime;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.Optional;
import java.util.UUID;

public interface IMinecraftVelocityProxyServer extends IDependencyInjectableInterface {
    Optional<RegisteredServer> getServer(String serverName);

    Optional<Player> getPlayer(UUID playerId);

    Optional<Player> getPlayer(String username);
}
