package com.tavall.hytale.resourcegame.frontend.minecraft.server;

import java.util.Collection;

public interface MinecraftBukkitServerView {
    String hostname();

    int maxPlayers();

    Collection<MinecraftBukkitPlayerView> onlinePlayers();
}
