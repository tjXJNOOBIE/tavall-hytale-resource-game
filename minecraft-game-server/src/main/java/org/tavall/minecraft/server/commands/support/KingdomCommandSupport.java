package org.tavall.minecraft.server.commands.support;

import org.tavall.api.minecraft.frontend.FrontendCommandVerificationResult;
import org.tavall.api.minecraft.MinecraftVisualRenderRequest;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendSurfaceIdentity;
import org.tavall.minecraft.framework.game.ui.UiScreenKey;
import org.tavall.minecraft.server.IMinecraftBukkitServerDomain;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class KingdomCommandSupport {
    private KingdomCommandSupport() {
    }

    public static boolean forward(IMinecraftBukkitServerDomain domain, CommandSender sender, String label, String rootToken, String[] args) {
        if (!sender.hasPermission("tavall.resourcegame.command")) {
            sender.sendMessage("Missing permission tavall.resourcegame.command.");
            return true;
        }
        String rawInput = rawKingdomInput(label, rootToken, args);
        Map<String, String> metadata = commandMetadata(domain, sender, label);
        try {
            FrontendCommandVerificationResult result = domain.getMinecraftBukkitCommandClientHandler().submitCommand(
                    platformAccountId(domain, sender),
                    sender.getName(),
                    rawInput,
                    "minecraft-bukkit-command-" + UUID.randomUUID(),
                    metadata
            );
            renderFeedback(domain, sender, rawInput, result);
        } catch (IOException exception) {
            domain.getMinecraftBukkitLogger().warning("Failed to submit Tavall Resource Game Bukkit kingdom command: " + exception.getMessage());
            sender.sendMessage(ChatColor.RED + "Command failed: " + exception.getMessage());
        }
        return true;
    }

    public static boolean openPageIfPlayer(IMinecraftBukkitServerDomain domain, CommandSender sender, UiScreenKey pageType, String feedback) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This page requires a player sender.");
            return true;
        }
        domain.getKingdomInventoryUiHandler().open(player, pageType, feedback);
        sender.sendMessage(ChatColor.GOLD + "Kingdom" + ChatColor.GRAY + ": " + ChatColor.GREEN + (feedback == null || feedback.isBlank() ? "Menu opened." : feedback));
        return true;
    }

    public static void renderFeedback(IMinecraftBukkitServerDomain domain, CommandSender sender, String rawInput, FrontendCommandVerificationResult result) {
        String actionMessage = null;
        if (result.success() && sender instanceof Player player) {
            actionMessage = domain.getMinecraftBukkitWorldActionHandler()
                    .apply(player, rawInput, result)
                    .orElse(null);
        }
        String message = actionMessage != null && !actionMessage.isBlank()
                ? actionMessage
                : (result.message() == null || result.message().isBlank() ? "Action unavailable." : result.message());
        ChatColor tone = result.success() ? ChatColor.GREEN : ChatColor.RED;
        sender.sendMessage(ChatColor.GOLD + "Kingdom" + ChatColor.GRAY + ": " + tone + message);
        String normalizedInput = rawInput.toLowerCase();
        if (sender instanceof Player player && (normalizedInput.startsWith("kd companion") || normalizedInput.startsWith("kingdom companion"))) {
            domain.getMinecraftBukkitVisualHandler().renderCompanionFeedback(player, rawInput, result);
        } else if (sender instanceof Player player) {
            domain.getMinecraftBukkitVisualHandler().renderKingdomCommandFeedback(player, rawInput, result);
        }
    }

    public static String platformAccountId(IMinecraftBukkitServerDomain domain, CommandSender sender) {
        if (sender instanceof Player player) {
            return player.getUniqueId().toString();
        }
        return "minecraft-console:" + domain.getMinecraftBukkitServerConfig().serverId();
    }

    public static String rawKingdomInput(String label, String rootToken, String[] args) {
        StringBuilder builder = new StringBuilder(normalizeKingdomAlias(label));
        if (rootToken != null && !rootToken.isBlank()) {
            builder.append(' ').append(rootToken);
        }
        for (String arg : args) {
            builder.append(' ').append(arg);
        }
        return builder.toString();
    }

    public static String normalizeKingdomAlias(String label) {
        return "kingdom".equalsIgnoreCase(label) ? "kingdom" : "kd";
    }

    public static Map<String, String> commandMetadata(IMinecraftBukkitServerDomain domain, CommandSender sender, String label) {
        Map<String, String> metadata = new LinkedHashMap<String, String>();
        metadata.put("serverId", domain.getMinecraftBukkitServerConfig().serverId());
        metadata.put("surfaceIdentity", "BUKKIT_SERVER");
        metadata.put("proxyId", domain.getMinecraftBukkitServerConfig().proxyId());
        metadata.put("alias", normalizeKingdomAlias(label));
        metadata.put("sourceType", sender instanceof Player ? "player" : "console");
        if (sender instanceof Player player) {
            metadata.put("worldName", player.getWorld().getName());
        }
        return metadata;
    }

    public static List<String> matching(List<String> candidates, String prefix) {
        String normalizedPrefix = prefix == null ? "" : prefix.toLowerCase();
        ArrayList<String> matches = new ArrayList<String>();
        for (String candidate : candidates) {
            if (candidate.toLowerCase().startsWith(normalizedPrefix)) {
                matches.add(candidate);
            }
        }
        matches.sort(String::compareTo);
        return matches;
    }
}
