package com.tavall.hytale.resourcegame.frontend.minecraft.server;

import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;

public final class MinecraftBukkitServerPlugin extends JavaPlugin implements Listener {
    private MinecraftBukkitServerConfig config;
    private MinecraftBukkitSnapshotHandler snapshotHandler;
    private MinecraftBukkitSnapshotClientHandler snapshotClientHandler;
    private MinecraftBukkitServerView serverView;
    private MinecraftBukkitVisualHandler visualHandler;

    @Override
    public void onEnable() {
        config = MinecraftBukkitServerConfig.fromEnvironment(System.getenv());
        MinecraftBukkitJsonHandler jsonHandler = new MinecraftBukkitJsonHandler();
        snapshotHandler = new MinecraftBukkitSnapshotHandler(config, System.currentTimeMillis());
        snapshotClientHandler = new MinecraftBukkitSnapshotClientHandler(config.snapshotIngressUri(), jsonHandler);
        serverView = new BukkitServerViewAdapter(getServer());
        visualHandler = new MinecraftBukkitVisualHandler();

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(
                new MinecraftBukkitInteractionHandler(
                        config,
                        new MinecraftBukkitCommandClientHandler(config.commandIngressUri(), jsonHandler),
                        getLogger()
                ),
                this
        );
        registerCommand();
        startSnapshotHeartbeat();
        getLogger().info("Tavall Resource Game Bukkit server frontend enabled. serverId=" + config.serverId()
                + " snapshotIngress=" + config.snapshotIngressUri());
    }

    @Override
    public void onDisable() {
        getLogger().info("Tavall Resource Game Bukkit server frontend disabled. serverId=" + config.serverId());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        visualHandler.renderJoinVisual(player, config.serverId());
        submitSnapshotQuietly();
    }

    private void registerCommand() {
        PluginCommand command = getCommand("tavallserver");
        if (command != null) {
            command.setExecutor(new MinecraftBukkitCommandHandler(
                    snapshotHandler,
                    snapshotClientHandler,
                    serverView,
                    visualHandler,
                    getLogger()
            ));
        }
    }

    private void startSnapshotHeartbeat() {
        new BukkitRunnable() {
            @Override
            public void run() {
                submitSnapshotQuietly();
            }
        }.runTaskTimerAsynchronously(MinecraftBukkitServerPlugin.this, 20L, Math.max(20L, config.snapshotIntervalTicks()));
    }

    private void submitSnapshotQuietly() {
        try {
            MinecraftBukkitServerSnapshot snapshot = snapshotHandler.createSnapshot(serverView, System.currentTimeMillis());
            boolean submitted = snapshotClientHandler.submitSnapshot(snapshot);
            if (!submitted) {
                getLogger().warning("Tavall Resource Game server snapshot was rejected. serverId=" + config.serverId());
            }
        } catch (IOException exception) {
            getLogger().warning("Failed to submit Tavall Resource Game server snapshot: " + exception.getMessage());
        }
    }
}
