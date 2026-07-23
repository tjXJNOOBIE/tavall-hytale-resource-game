package org.tavall.minecraft.server.tasks;

import org.tavall.dependency.IDependencyInjectableConcrete;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.tavall.minecraft.server.MinecraftBukkitServerPlugin;

public final class MinecraftBukkitTaskSchedulerHandler implements IMinecraftBukkitTaskScheduler, IDependencyInjectableConcrete {
    @Override
    public void runAsync(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(MinecraftBukkitServerPlugin.class), runnable);
    }

    @Override
    public void runLater(Runnable runnable, long delayTicks) {
        Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(MinecraftBukkitServerPlugin.class), runnable, Math.max(0L, delayTicks));
    }
}
