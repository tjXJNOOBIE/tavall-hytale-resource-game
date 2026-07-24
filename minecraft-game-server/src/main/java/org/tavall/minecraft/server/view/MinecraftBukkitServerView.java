package org.tavall.minecraft.server.view;

import org.tavall.dependency.IDependencyInjectableInterface;

import java.util.Collection;

public interface MinecraftBukkitServerView extends IDependencyInjectableInterface {
    String hostname();

    int maxPlayers();

    Collection<MinecraftBukkitPlayerView> onlinePlayers();
}
