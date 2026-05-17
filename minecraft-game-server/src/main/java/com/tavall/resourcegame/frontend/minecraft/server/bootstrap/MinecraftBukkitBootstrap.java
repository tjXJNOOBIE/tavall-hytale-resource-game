package com.tavall.resourcegame.frontend.minecraft.server.bootstrap;

import com.tavall.resourcegame.frontend.minecraft.server.IMinecraftBukkitLogger;
import com.tavall.resourcegame.frontend.minecraft.server.IMinecraftBukkitRuntimeState;
import com.tavall.resourcegame.frontend.minecraft.server.IMinecraftBukkitServerConfig;
import com.tavall.resourcegame.frontend.minecraft.server.MinecraftBukkitLoggerHandler;
import com.tavall.resourcegame.frontend.minecraft.server.MinecraftBukkitRuntimeState;
import com.tavall.resourcegame.frontend.minecraft.server.MinecraftBukkitServerDependencyModule;
import com.tavall.resourcegame.frontend.minecraft.server.MinecraftBukkitServerPlugin;
import com.tavall.resourcegame.frontend.minecraft.server.MinecraftBukkitCommandHandler;
import com.tavall.resourcegame.frontend.minecraft.server.MinecraftBukkitInteractionHandler;
import com.tavall.resourcegame.frontend.minecraft.server.MinecraftBukkitInteractionMenuHandler;
import com.tavall.resourcegame.frontend.minecraft.server.MinecraftBukkitInteractionSessionTracker;
import com.tavall.resourcegame.frontend.minecraft.server.MinecraftBukkitInteractionTargetResolver;
import com.tavall.resourcegame.frontend.minecraft.server.MinecraftBukkitInventoryUiHandler;
import com.tavall.resourcegame.frontend.minecraft.server.MinecraftBukkitPlayerJoinHandler;
import com.tavall.resourcegame.frontend.minecraft.server.MinecraftBukkitPopulationWorldActionHandler;
import com.tavall.resourcegame.frontend.minecraft.server.MinecraftBukkitStructureWorldActionHandler;
import com.tavall.resourcegame.frontend.minecraft.server.MinecraftBukkitTaskSchedulerHandler;
import com.tavall.resourcegame.frontend.minecraft.server.MinecraftBukkitWorldActionHandler;
import com.tavall.resourcegame.frontend.minecraft.server.protection.MinecraftBukkitStructureProtectionHandler;
import com.tavall.resourcegame.frontend.minecraft.server.view.BukkitServerViewAdapter;
import com.tavall.resourcegame.frontend.minecraft.server.view.MinecraftBukkitServerView;
import com.tavall.resourcegame.frontend.minecraft.server.visual.MinecraftBukkitVisualHandler;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.bukkit.command.PluginCommand;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public final class MinecraftBukkitBootstrap {
    private final MinecraftBukkitServerPlugin plugin;
    private BukkitRunnable snapshotHeartbeat;

    public MinecraftBukkitBootstrap(MinecraftBukkitServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        DependencyLoaderAccess.registerInstance(IMinecraftBukkitServerConfig.class, plugin.getMinecraftBukkitServerConfig());
        DependencyLoaderAccess.registerInstance(IMinecraftBukkitRuntimeState.class, new MinecraftBukkitRuntimeState(System.currentTimeMillis()));
        DependencyLoaderAccess.registerInstance(MinecraftBukkitServerView.class, new BukkitServerViewAdapter(plugin.getServer()));
        DependencyLoaderAccess.registerInstance(IMinecraftBukkitLogger.class, new MinecraftBukkitLoggerHandler());
        new MinecraftBukkitServerDependencyModule().registerDependencies();
        registerListeners();
        plugin.getMinecraftBukkitResourcePackHandler().ensureLayout();
        registerCommands();
        startSnapshotHeartbeat();
        plugin.getLogger().info("Tavall Resource Game Bukkit server frontend enabled. serverId=" + plugin.getMinecraftBukkitServerConfig().serverId()
                + " proxyId=" + plugin.getMinecraftBukkitServerConfig().proxyId()
                + " resourcePackPath=" + plugin.getMinecraftBukkitResourcePackHandler().statusLine());
    }

    public void shutdown() {
        if (snapshotHeartbeat != null) {
            try {
                snapshotHeartbeat.cancel();
            } catch (Throwable ignored) {
            }
        }
        plugin.getLogger().info("Tavall Resource Game Bukkit server frontend disabled.");
    }

    private void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(plugin.getMinecraftBukkitPlayerJoinHandler(), plugin);
        plugin.getServer().getPluginManager().registerEvents(plugin.getMinecraftBukkitInteractionHandler(), plugin);
        plugin.getServer().getPluginManager().registerEvents(plugin.getMinecraftBukkitInteractionMenuHandler(), plugin);
        plugin.getServer().getPluginManager().registerEvents(plugin.getMinecraftBukkitStructureProtectionHandler(), plugin);
        plugin.getServer().getPluginManager().registerEvents(plugin.getKingdomInventoryUiHandler(), plugin);
    }

    private void registerCommands() {
        registerCommand("tavallserver");
        registerCommand("kd");
        registerCommand("kingdom");
    }

    private void registerCommand(String commandName) {
        PluginCommand command = plugin.getCommand(commandName);
        if (command != null) {
            command.setExecutor(plugin.getMinecraftBukkitCommandHandler());
            command.setTabCompleter(plugin.getMinecraftBukkitCommandHandler());
        }
    }

    private void startSnapshotHeartbeat() {
        snapshotHeartbeat = new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getMinecraftBukkitSnapshotSubmitHandler().submitSnapshotQuietly();
            }
        };
        snapshotHeartbeat.runTaskTimerAsynchronously(plugin, 20L, Math.max(20L, plugin.getMinecraftBukkitServerConfig().snapshotIntervalTicks()));
    }
}
