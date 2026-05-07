package com.tavall.hytale.resourcegame.frontend.minecraft.server;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class MinecraftBukkitVisualHandler {
    public void renderJoinVisual(Player player, String serverId) {
        player.sendMessage(ChatColor.GOLD + "Tavall Resource Game" + ChatColor.GRAY + " server surface online: " + ChatColor.WHITE + serverId);
    }

    public void renderSnapshotSubmitted(Player player, boolean submitted) {
        if (submitted) {
            player.sendMessage(ChatColor.GREEN + "Resource-game server snapshot submitted.");
            return;
        }
        player.sendMessage(ChatColor.RED + "Resource-game server snapshot was not accepted.");
    }
}
