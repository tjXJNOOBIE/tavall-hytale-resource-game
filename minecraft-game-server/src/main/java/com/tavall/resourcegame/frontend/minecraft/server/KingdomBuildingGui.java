package org.tavall.minecraft.server;

import org.tavall.api.minecraft.interaction.InteractionRequest;
import org.tavall.api.minecraft.interaction.InteractionResult;
import org.tavall.api.minecraft.interaction.InteractionTargetType;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class KingdomBuildingGui implements IMinecraftBukkitServerDomain, IDependencyInjectableConcrete {
    public boolean open(Player player) {
        Optional<MinecraftBukkitInteractionTarget> target = getMinecraftBukkitInteractionSessionTracker().current(player.getUniqueId());
        if (target.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Look at a building or NPC first, then reopen the menu.");
            return true;
        }

        return open(player, target.get());
    }

    private boolean open(Player player, MinecraftBukkitInteractionTarget target) {
        Map<String, String> context = new LinkedHashMap<String, String>(target.metadata());
        context.put("sourceCommand", "kd ui building");
        context.put("menuHint", "building");
        context.put("targetType", target.targetType().name());
        context.put("targetId", target.targetId());
        try {
            InteractionResult result = getMinecraftBukkitCommandClientHandler().submitInteraction(new InteractionRequest(
                    "minecraft-building-ui-" + UUID.randomUUID(),
                    player.getUniqueId().toString(),
                    target.targetType(),
                    target.targetId(),
                    "open_building_menu",
                    getMinecraftBukkitServerConfig().serverId(),
                    player.getWorld().getName(),
                    context,
                    Instant.now().toEpochMilli()
            ));
            if (result.menu() != null) {
                getMinecraftBukkitInteractionMenuHandler().open(player, result.menu(), result.message());
                return true;
            }
            player.sendMessage((result.success() ? ChatColor.GREEN : ChatColor.RED) + result.message());
        } catch (IOException exception) {
            getMinecraftBukkitLogger().warning("Failed to open building menu via control plane: " + exception.getMessage());
            player.sendMessage(ChatColor.RED + "Building menu unavailable: " + exception.getMessage());
        }
        return true;
    }
}
