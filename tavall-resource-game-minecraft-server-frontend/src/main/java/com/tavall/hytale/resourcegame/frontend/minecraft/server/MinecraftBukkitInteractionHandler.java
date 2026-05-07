package com.tavall.hytale.resourcegame.frontend.minecraft.server;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public final class MinecraftBukkitInteractionHandler implements Listener {
    private final MinecraftBukkitServerConfig config;
    private final MinecraftBukkitCommandClientHandler commandClientHandler;
    private final Logger logger;

    public MinecraftBukkitInteractionHandler(
            MinecraftBukkitServerConfig config,
            MinecraftBukkitCommandClientHandler commandClientHandler,
            Logger logger
    ) {
        this.config = config;
        this.commandClientHandler = commandClientHandler;
        this.logger = logger;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        Map<String, String> arguments = new LinkedHashMap<String, String>();
        arguments.put("entityId", entity.getUniqueId().toString());
        arguments.put("entityType", entity.getType().name());
        arguments.put("worldName", entity.getWorld().getName());

        Map<String, String> metadata = new LinkedHashMap<String, String>();
        metadata.put("serverId", config.serverId());
        metadata.put("surfaceIdentity", "BUKKIT_SERVER");
        metadata.put("proxyId", config.proxyId());

        try {
            commandClientHandler.submitInteraction(
                    player.getUniqueId().toString(),
                    player.getName(),
                    "minecraft.entity.interact",
                    arguments,
                    "minecraft-bukkit-interact-" + UUID.randomUUID(),
                    metadata
            );
        } catch (IOException exception) {
            logger.warning("Failed to submit Minecraft entity interaction to control ingress: " + exception.getMessage());
        }
    }
}
