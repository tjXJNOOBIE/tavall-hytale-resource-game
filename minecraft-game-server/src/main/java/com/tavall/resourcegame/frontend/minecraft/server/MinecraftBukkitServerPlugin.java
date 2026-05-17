package com.tavall.resourcegame.frontend.minecraft.server;

import com.tavall.resourcegame.frontend.minecraft.server.bootstrap.MinecraftBukkitBootstrap;
import org.bukkit.plugin.java.JavaPlugin;

public final class MinecraftBukkitServerPlugin extends JavaPlugin implements IMinecraftBukkitServerDomain {
    private MinecraftBukkitBootstrap bootstrap;

    @Override
    public void onEnable() {
        bootstrap = new MinecraftBukkitBootstrap(this);
        bootstrap.initialize();
    }

    @Override
    public void onDisable() {
        if (bootstrap != null) {
            bootstrap.shutdown();
        }
    }
}
