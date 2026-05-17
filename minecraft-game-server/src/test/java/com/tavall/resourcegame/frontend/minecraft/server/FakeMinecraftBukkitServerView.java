package com.tavall.resourcegame.frontend.minecraft.server;

import java.util.Collection;
import java.util.List;

final class FakeMinecraftBukkitServerView implements MinecraftBukkitServerView {
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
        return List.of(new FakeMinecraftBukkitPlayerView());
    }
}
