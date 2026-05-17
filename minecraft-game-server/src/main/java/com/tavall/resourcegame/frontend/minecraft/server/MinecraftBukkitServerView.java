package com.tavall.resourcegame.frontend.minecraft.server;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.util.Collection;

public interface MinecraftBukkitServerView extends IDependencyInjectableInterface {
    String hostname();

    int maxPlayers();

    Collection<MinecraftBukkitPlayerView> onlinePlayers();
}
