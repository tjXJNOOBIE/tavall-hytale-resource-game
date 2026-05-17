package com.tavall.resourcegame.frontend.minecraft.server;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class MinecraftBukkitServerPlugin extends JavaPlugin implements IMinecraftBukkitServerDomain {
    @Override
    public void onEnable() {
        DependencyLoaderAccess.registerInstance(IMinecraftBukkitServerConfig.class, MinecraftBukkitServerConfig.fromEnvironment(System.getenv()));
        DependencyLoaderAccess.registerInstance(IMinecraftBukkitRuntimeState.class, new MinecraftBukkitRuntimeState(System.currentTimeMillis()));
        DependencyLoaderAccess.registerInstance(MinecraftBukkitServerView.class, new BukkitServerViewAdapter(getServer()));
        DependencyLoaderAccess.registerInstance(IMinecraftBukkitLogger.class, new MinecraftBukkitLoggerHandler());
        new MinecraftBukkitServerDependencyModule().registerDependencies();

        getServer().getPluginManager().registerEvents(getMinecraftBukkitPlayerJoinHandler(), this);
        getServer().getPluginManager().registerEvents(getMinecraftBukkitInteractionHandler(), this);
        getServer().getPluginManager().registerEvents(getMinecraftBukkitInteractionMenuHandler(), this);
        getServer().getPluginManager().registerEvents(getMinecraftBukkitStructureProtectionHandler(), this);
        getServer().getPluginManager().registerEvents(getKingdomInventoryUiHandler(), this);
        getMinecraftBukkitResourcePackHandler().ensureLayout();
        registerCommand();
        startSnapshotHeartbeat();
        getLogger().info("Tavall Resource Game Bukkit server frontend enabled. serverId=" + getMinecraftBukkitServerConfig().serverId()
                + " proxyId=" + getMinecraftBukkitServerConfig().proxyId()
                + " resourcePackPath=" + getMinecraftBukkitResourcePackHandler().statusLine());
    }

    @Override
    public void onDisable() {
        getLogger().info("Tavall Resource Game Bukkit server frontend disabled.");
    }

    private void registerCommand() {
        registerCommand("tavallserver");
        registerCommand("kd");
        registerCommand("kingdom");
    }

    private void registerCommand(String commandName) {
        PluginCommand command = getCommand(commandName);
        if (command != null) {
            command.setExecutor(getMinecraftBukkitCommandHandler());
            command.setTabCompleter(getMinecraftBukkitCommandHandler());
        }
    }

    private void startSnapshotHeartbeat() {
        new BukkitRunnable() {
            @Override
            public void run() {
                getMinecraftBukkitSnapshotSubmitHandler().submitSnapshotQuietly();
            }
        }.runTaskTimerAsynchronously(MinecraftBukkitServerPlugin.this, 20L, Math.max(20L, getMinecraftBukkitServerConfig().snapshotIntervalTicks()));
    }
}
