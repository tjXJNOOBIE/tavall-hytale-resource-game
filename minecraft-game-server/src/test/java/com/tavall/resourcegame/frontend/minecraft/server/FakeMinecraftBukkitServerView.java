package org.tavall.minecraft.server;

import org.tavall.minecraft.server.view.MinecraftBukkitPlayerView;
import org.tavall.minecraft.server.view.MinecraftBukkitServerView;

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
