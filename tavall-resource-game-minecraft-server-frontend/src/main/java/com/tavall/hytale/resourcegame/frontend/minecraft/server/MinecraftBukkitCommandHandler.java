package com.tavall.hytale.resourcegame.frontend.minecraft.server;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.logging.Logger;

public final class MinecraftBukkitCommandHandler implements CommandExecutor {
    private final MinecraftBukkitSnapshotHandler snapshotHandler;
    private final MinecraftBukkitSnapshotClientHandler snapshotClientHandler;
    private final MinecraftBukkitServerView serverView;
    private final MinecraftBukkitVisualHandler visualHandler;
    private final Logger logger;

    public MinecraftBukkitCommandHandler(
            MinecraftBukkitSnapshotHandler snapshotHandler,
            MinecraftBukkitSnapshotClientHandler snapshotClientHandler,
            MinecraftBukkitServerView serverView,
            MinecraftBukkitVisualHandler visualHandler,
            Logger logger
    ) {
        this.snapshotHandler = snapshotHandler;
        this.snapshotClientHandler = snapshotClientHandler;
        this.serverView = serverView;
        this.visualHandler = visualHandler;
        this.logger = logger;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            MinecraftBukkitServerSnapshot snapshot = snapshotHandler.createSnapshot(serverView, System.currentTimeMillis());
            boolean submitted = snapshotClientHandler.submitSnapshot(snapshot);
            sender.sendMessage("Tavall Resource Game server surface players=" + snapshot.getOnlinePlayerCount() + " submitted=" + submitted);
            if (sender instanceof Player) {
                visualHandler.renderSnapshotSubmitted((Player) sender, submitted);
            }
        } catch (IOException exception) {
            logger.warning("Failed to submit Tavall Resource Game server snapshot: " + exception.getMessage());
            sender.sendMessage("Tavall Resource Game server snapshot failed: " + exception.getMessage());
        }
        return true;
    }
}
