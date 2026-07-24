package org.tavall.minecraft.server.visual;

import org.tavall.api.minecraft.frontend.FrontendCommandVerificationResult;
import org.tavall.api.minecraft.MinecraftVisualRenderRequest;
import org.tavall.dependency.IDependencyInjectableConcrete;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

public final class MinecraftBukkitVisualHandler implements IMinecraftBukkitVisualHandler, IDependencyInjectableConcrete {
    @Override
    public void renderJoinVisual(Player player, String serverId) {
        player.sendMessage(ChatColor.GOLD + "Tavall Resource Game" + ChatColor.GRAY + " server surface online: " + ChatColor.WHITE + serverId);
    }

    @Override
    public void renderSnapshotSubmitted(Player player, boolean submitted) {
        if (submitted) {
            player.sendMessage(ChatColor.GREEN + "Resource-game server snapshot submitted.");
            return;
        }
        player.sendMessage(ChatColor.RED + "Resource-game server snapshot was not accepted.");
    }

    @Override
    public void renderVisualRequest(Player player, MinecraftVisualRenderRequest request) {
        String title = request.title() == null || request.title().isBlank() ? "Tavall Resource Game" : request.title();
        String body = request.body() == null || request.body().isBlank() ? request.visualType() : request.body();
        if ("TITLE".equalsIgnoreCase(request.visualType())) {
            player.sendTitle(ChatColor.GOLD + title, ChatColor.WHITE + body, 10, 60, 10);
            return;
        }
        player.sendMessage(ChatColor.GOLD + title + ChatColor.GRAY + ": " + ChatColor.WHITE + body);
    }

    @Override
    public void renderKingdomCommandFeedback(Player player, String commandLine, FrontendCommandVerificationResult result) {
        String normalized = commandLine == null ? "" : commandLine.toLowerCase();
        if (normalized.contains("companion ")) {
            renderCompanionFeedback(player, commandLine, result);
            return;
        }

        Location location = player.getLocation().add(0.0, 1.0, 0.0);
        if (normalized.contains("castle ")) {
            if (result.success()) {
                player.playSound(location, Sound.BLOCK_BEACON_POWER_SELECT, 0.9f, 1.0f);
                pulseBlock(player, location, Material.STONE_BRICKS.createBlockData(), 18);
                player.sendTitle(ChatColor.GOLD + "Castle Order", ChatColor.WHITE + "Stone and route lines respond.", 10, 45, 10);
                return;
            }
            player.playSound(location, Sound.BLOCK_ANVIL_LAND, 0.7f, 0.7f);
            return;
        }

        if (normalized.contains("citizens ")) {
            if (result.success()) {
                player.playSound(location, Sound.ENTITY_VILLAGER_YES, 1.0f, 1.1f);
                player.spawnParticle(Particle.HAPPY_VILLAGER, location, 16, 0.45, 0.55, 0.45, 0.02);
                player.sendTitle(ChatColor.GOLD + "Citizens", ChatColor.WHITE + "The settlement responds.", 10, 45, 10);
                return;
            }
            player.playSound(location, Sound.ENTITY_VILLAGER_NO, 0.8f, 0.9f);
            return;
        }

        if (normalized.contains("resources ")) {
            if (result.success()) {
                player.playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.25f);
                pulseBlock(player, location, Material.GLOWSTONE.createBlockData(), 14);
                player.sendTitle(ChatColor.GOLD + "Supplies", ChatColor.WHITE + "Stockpiles have changed.", 10, 40, 10);
                return;
            }
            player.playSound(location, Sound.BLOCK_CHAIN_BREAK, 0.7f, 0.7f);
            return;
        }

        if (normalized.contains("clock ") || normalized.contains("schedule ") || normalized.contains("aging ")) {
            if (result.success()) {
                player.playSound(location, Sound.BLOCK_BELL_USE, 0.9f, 1.0f);
                player.spawnParticle(Particle.END_ROD, location, 14, 0.35, 0.45, 0.35, 0.01);
                player.sendTitle(ChatColor.GOLD + "Clock Shift", ChatColor.WHITE + "Time and routine move forward.", 10, 45, 10);
                return;
            }
            player.playSound(location, Sound.BLOCK_BELL_RESONATE, 0.7f, 0.8f);
            return;
        }

        if (normalized.contains("kingdom ") || normalized.contains("ui ") || normalized.contains("scene ") || normalized.contains("bootstrap")) {
            if (result.success()) {
                player.playSound(location, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.0f);
                player.spawnParticle(Particle.ENCHANT, location, 12, 0.35, 0.45, 0.35, 0.1);
                player.sendTitle(ChatColor.GOLD + "Kingdom View", ChatColor.WHITE + "The control surface refreshed.", 10, 40, 10);
                return;
            }
            player.playSound(location, Sound.BLOCK_NOTE_BLOCK_BASS, 0.7f, 0.7f);
            return;
        }

        if (normalized.contains("nodes ") || normalized.contains("buildings ") || normalized.contains("building ") || normalized.contains("place ")) {
            if (result.success()) {
                player.playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.1f);
                pulseBlock(player, location, Material.IRON_BLOCK.createBlockData(), 10);
                player.sendTitle(ChatColor.GOLD + "Worksite", ChatColor.WHITE + "Placement and structure changed.", 10, 40, 10);
                return;
            }
            player.playSound(location, Sound.BLOCK_ANVIL_USE, 0.75f, 0.8f);
            return;
        }

        if (normalized.contains("npc ") || normalized.contains("interior ") || normalized.contains("focus ") || normalized.contains("interact ") || normalized.contains("scan ")) {
            if (result.success()) {
                player.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 0.75f, 1.15f);
                player.spawnParticle(Particle.PORTAL, location, 20, 0.45, 0.55, 0.45, 0.15);
                player.sendTitle(ChatColor.GOLD + "Action Point", ChatColor.WHITE + "The target is live.", 10, 40, 10);
                return;
            }
            player.playSound(location, Sound.BLOCK_NOTE_BLOCK_BASS, 0.7f, 0.7f);
            return;
        }

        if (result.success()) {
            player.playSound(location, Sound.BLOCK_NOTE_BLOCK_PLING, 0.6f, 1.0f);
            return;
        }
        player.playSound(location, Sound.BLOCK_NOTE_BLOCK_BASS, 0.6f, 0.8f);
    }

    @Override
    public void renderCompanionFeedback(Player player, String commandLine, FrontendCommandVerificationResult result) {
        String normalized = commandLine == null ? "" : commandLine.toLowerCase();
        Location location = player.getLocation().add(0.0, 1.0, 0.0);
        if (normalized.contains("companion give") || normalized.contains("companion create")) {
            player.playSound(location, Sound.ENTITY_ALLAY_AMBIENT_WITH_ITEM, 1.0f, 1.0f);
            player.spawnParticle(Particle.HAPPY_VILLAGER, location, 18, 0.45, 0.55, 0.45, 0.02);
            player.sendTitle(ChatColor.GOLD + "Companion Joined", ChatColor.WHITE + "Your retinue grows.", 10, 50, 10);
            return;
        }
        if (normalized.contains("companion train") || normalized.contains("companion skill unlock") || normalized.contains("companion skill upgrade")) {
            player.playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.15f);
            player.spawnParticle(Particle.ENCHANT, location, 24, 0.55, 0.8, 0.55, 0.1);
            player.sendTitle(ChatColor.GOLD + "Companion Empowered", ChatColor.WHITE + "Training takes hold.", 10, 50, 10);
            return;
        }
        if (normalized.contains("companion summon") || normalized.contains("companion recall")) {
            player.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.2f);
            player.spawnParticle(Particle.PORTAL, location, 28, 0.45, 0.8, 0.45, 0.15);
            player.sendTitle(ChatColor.GOLD + "Companion Called", ChatColor.WHITE + "The bond answers.", 10, 50, 10);
            return;
        }
        if (normalized.contains("companion wall assign") || normalized.contains("companion wall remove")) {
            player.playSound(location, Sound.BLOCK_BEACON_POWER_SELECT, 0.9f, 1.0f);
            player.spawnParticle(Particle.BLOCK, location, 16, 0.35, 0.45, 0.35, 0.08, player.getLocation().getBlock().getBlockData());
            player.sendTitle(ChatColor.GOLD + "Wall Order", ChatColor.WHITE + "Companion deployment updated.", 10, 40, 10);
            return;
        }
        player.playSound(location, Sound.BLOCK_NOTE_BLOCK_PLING, 0.6f, 1.0f);
    }

    private void pulseBlock(Player player, Location location, BlockData blockData, int count) {
        player.spawnParticle(Particle.BLOCK, location, count, 0.35, 0.45, 0.35, 0.05, blockData);
    }
}
