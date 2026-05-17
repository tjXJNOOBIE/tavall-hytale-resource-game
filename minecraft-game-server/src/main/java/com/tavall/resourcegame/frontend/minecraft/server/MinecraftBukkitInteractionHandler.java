package com.tavall.resourcegame.frontend.minecraft.server;

import com.tavall.resourcegame.api.internal.interaction.InteractionRequest;
import com.tavall.resourcegame.api.internal.interaction.InteractionResult;
import com.tavall.resourcegame.api.internal.interaction.InteractionTargetType;
import com.tavall.resourcegame.ui.UiPageType;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class MinecraftBukkitInteractionHandler implements IMinecraftBukkitInteractionHandler, IMinecraftBukkitServerDomain, IDependencyInjectableConcrete {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        Optional<MinecraftBukkitInteractionTarget> target = getMinecraftBukkitInteractionTargetResolver().resolve(entity);
        if (target.isEmpty()) {
            return;
        }

        event.setCancelled(true);
        MinecraftBukkitInteractionTarget resolvedTarget = target.get();
        getMinecraftBukkitInteractionSessionTracker().remember(player.getUniqueId(), resolvedTarget);
        if ("castle".equalsIgnoreCase(resolvedTarget.metadata().getOrDefault("structureKind", ""))) {
            getKingdomInventoryUiHandler().open(player, UiPageType.CASTLE_MAIN, resolvedTarget.displayName() + " overview.");
            return;
        }

        try {
            InteractionResult result = getMinecraftBukkitCommandClientHandler().submitInteraction(openRequest(player, resolvedTarget));
            if (result.menu() != null) {
                getMinecraftBukkitInteractionMenuHandler().open(player, result.menu(), result.message());
                return;
            }
            if (result.success()) {
                player.sendMessage(ChatColor.GREEN + result.message());
                return;
            }
            String message = result.message();
            if (result.disabledReason() != null && !result.disabledReason().isBlank()) {
                message = message + " " + result.disabledReason();
            }
            player.sendMessage(ChatColor.RED + message);
        } catch (IOException exception) {
            player.sendMessage(ChatColor.RED + "NPC/building interaction unavailable: " + exception.getMessage());
        }
    }

    private InteractionRequest openRequest(Player player, MinecraftBukkitInteractionTarget target) {
        Map<String, String> context = new LinkedHashMap<String, String>(target.metadata());
        context.put("targetType", target.targetType().name());
        context.put("targetId", target.targetId());
        context.put("displayName", target.displayName());
        context.put("entityId", target.targetId());
        context.put("serverId", getMinecraftBukkitServerConfig().serverId());
        context.put("worldName", player.getWorld().getName());
        return new InteractionRequest(
                "minecraft-interaction-open-" + UUID.randomUUID(),
                player.getUniqueId().toString(),
                target.targetType(),
                target.targetId(),
                "open_menu",
                getMinecraftBukkitServerConfig().serverId(),
                player.getWorld().getName(),
                context,
                Instant.now().toEpochMilli()
        );
    }
}
