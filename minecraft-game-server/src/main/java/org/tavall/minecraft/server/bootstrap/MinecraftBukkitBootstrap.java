package org.tavall.minecraft.server.bootstrap;

import org.tavall.minecraft.server.IMinecraftBukkitRuntimeState;
import org.tavall.minecraft.server.IMinecraftBukkitServerConfig;
import org.tavall.minecraft.server.MinecraftBukkitRuntimeState;
import org.tavall.minecraft.server.MinecraftBukkitServerDependencyModule;
import org.tavall.minecraft.server.MinecraftBukkitServerPlugin;
import org.tavall.minecraft.server.MinecraftBukkitCommandHandler;
import org.tavall.minecraft.server.MinecraftBukkitInteractionHandler;
import org.tavall.minecraft.server.MinecraftBukkitInteractionMenuHandler;
import org.tavall.minecraft.server.MinecraftBukkitInteractionSessionTracker;
import org.tavall.minecraft.server.MinecraftBukkitInteractionTargetResolver;
import org.tavall.minecraft.server.MinecraftBukkitInventoryUiHandler;
import org.tavall.minecraft.server.MinecraftBukkitPlayerJoinHandler;
import org.tavall.minecraft.server.MinecraftBukkitPopulationWorldActionHandler;
import org.tavall.minecraft.server.MinecraftBukkitStructureWorldActionHandler;
import org.tavall.minecraft.server.MinecraftBukkitWorldActionHandler;
import org.tavall.minecraft.server.protection.MinecraftBukkitStructureProtectionHandler;
import org.tavall.minecraft.server.logging.MinecraftBukkitLoggerHandler;
import org.tavall.minecraft.server.logging.IMinecraftBukkitLogger;
import org.tavall.minecraft.server.tasks.MinecraftBukkitTaskSchedulerHandler;
import org.tavall.minecraft.server.view.BukkitServerViewAdapter;
import org.tavall.minecraft.server.view.MinecraftBukkitServerView;
import org.tavall.minecraft.server.visual.MinecraftBukkitVisualHandler;
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
        DependencyLoaderAccess.registerInstance(IMinecraftBukkitRuntimeState.class, new MinecraftBukkitRuntimeState(System.currentTimeMillis()));
        DependencyLoaderAccess.registerInstance(MinecraftBukkitServerView.class, new BukkitServerViewAdapter(plugin.getServer()));
        DependencyLoaderAccess.registerInstance(IMinecraftBukkitLogger.class, new MinecraftBukkitLoggerHandler());
        new MinecraftBukkitServerDependencyModule().registerDependencies();
        registerListeners();
        plugin.getMinecraftBukkitResourcePackHandler().ensureLayout();
        plugin.getMinecraftBukkitResourcePackHandler().startHostedPackServer();
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
        try {
            plugin.getMinecraftBukkitResourcePackHandler().stopHostedPackServer();
        } catch (Throwable ignored) {
        }
        plugin.getLogger().info("Tavall Resource Game Bukkit server frontend disabled.");
    }

    private void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(plugin.getMinecraftBukkitPlayerJoinHandler(), plugin);
        plugin.getServer().getPluginManager().registerEvents(plugin.getMinecraftBukkitResourcePackStatusHandler(), plugin);
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
