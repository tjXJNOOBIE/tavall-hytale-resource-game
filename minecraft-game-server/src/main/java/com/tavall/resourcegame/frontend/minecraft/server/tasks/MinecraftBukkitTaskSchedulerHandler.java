package com.tavall.resourcegame.frontend.minecraft.server.tasks;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import com.tavall.resourcegame.frontend.minecraft.server.MinecraftBukkitServerPlugin;

public final class MinecraftBukkitTaskSchedulerHandler implements IMinecraftBukkitTaskScheduler, IDependencyInjectableConcrete {
    @Override
    public void runAsync(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(MinecraftBukkitServerPlugin.class), runnable);
    }
}
