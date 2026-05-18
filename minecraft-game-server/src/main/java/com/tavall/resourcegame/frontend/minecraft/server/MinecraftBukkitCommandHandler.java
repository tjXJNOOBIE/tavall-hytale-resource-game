package org.tavall.minecraft.server;

import org.tavall.api.minecraft.frontend.FrontendCommandVerificationResult;
import org.tavall.api.minecraft.interaction.InteractionRequest;
import org.tavall.api.minecraft.interaction.InteractionResult;
import org.tavall.api.minecraft.interaction.InteractionTargetType;
import org.tavall.api.minecraft.MinecraftServerRuntimeSnapshot;
import org.tavall.api.minecraft.MinecraftVisualRenderRequest;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendSurfaceIdentity;
import org.tavall.minecraft.server.commands.support.KingdomCommandSupport;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class MinecraftBukkitCommandHandler implements IMinecraftBukkitCommandHandler, IMinecraftBukkitServerDomain, IDependencyInjectableConcrete {
    private static final Map<String, List<String>> KINGDOM_COMMAND_CATALOG = Map.ofEntries(
            Map.entry("ui", List.of("castle", "companion", "building", "npc", "debug", "liveops")),
            Map.entry("data", List.of("status")),
            Map.entry("castle", List.of("goto", "align", "move", "open", "refresh")),
            Map.entry("interior", List.of("exit", "add", "generate", "rebuild", "regen", "move", "delete")),
            Map.entry("citizens", List.of("summary", "spawn", "debug", "setjob", "train", "promote", "demote", "refresh-cache", "refresh-displays", "add", "set")),
            Map.entry("troops", List.of("debug", "wound", "heal", "add", "set")),
            Map.entry("resources", List.of("give", "add", "set")),
            Map.entry("account", List.of("status", "addxp", "setlevel", "debug")),
            Map.entry("hologram", List.of("spawn", "stack", "status", "clear")),
            Map.entry("nodes", List.of("goto", "align", "status", "place", "list", "select", "assign", "add", "pillage", "stock", "recall", "remove", "clear")),
            Map.entry("place", List.of("castle", "node", "building", "confirm", "cancel", "status", "preview", "move")),
            Map.entry("focus", List.of()),
            Map.entry("interact", List.of()),
            Map.entry("scan", List.of()),
            Map.entry("scene", List.of("refresh")),
            Map.entry("bootstrap", List.of()),
            Map.entry("tick", List.of("run", "healing", "clock")),
            Map.entry("kingdom", List.of("create", "debug", "scaling", "border")),
            Map.entry("coord", List.of("convert", "params")),
            Map.entry("instance", List.of("register", "health", "switch", "routing", "confirm", "fail")),
            Map.entry("params", List.of("list", "get", "set", "dry-run")),
            Map.entry("clock", List.of("state", "override", "mode", "config")),
            Map.entry("schedule", List.of("active", "create")),
            Map.entry("aging", List.of("policy", "tick")),
            Map.entry("tutorial", List.of("reset")),
            Map.entry("buildings", List.of("place", "stage", "spawn", "list", "status", "select", "align", "goto", "upgrade", "cancel", "finish", "clear")),
            Map.entry("building", List.of("open", "overview", "storage", "production", "upgrade", "close")),
            Map.entry("npc", List.of("open", "debug", "building", "close")),
            Map.entry("entity", List.of("spawn", "clear", "list")),
            Map.entry("companion", List.of("list", "give", "create", "debug", "setlevel", "xp", "morale", "behavior", "train", "claim", "cancel", "skill", "summon", "recall", "wall", "ui", "projection")),
            Map.entry("trade", List.of("status")),
            Map.entry("market", List.of("status")),
            Map.entry("scout", List.of("status")),
            Map.entry("recon", List.of("status")),
            Map.entry("intel", List.of("status")),
            Map.entry("retaliation", List.of("status"))
    );

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (isKingdomCommand(command.getName()) || isKingdomCommand(label)) {
            return getKingdomCommandRouter().onCommand(sender, command, label, args);
        }
        if (args.length > 0 && "visual".equalsIgnoreCase(args[0])) {
            return renderVisualCommand(sender, args);
        }
        if (args.length > 0 && "interact".equalsIgnoreCase(args[0])) {
            return submitInteractionCommand(sender, args);
        }
        try {
            MinecraftServerRuntimeSnapshot snapshot = getMinecraftBukkitSnapshotHandler()
                    .createSnapshot(getMinecraftBukkitServerView(), System.currentTimeMillis());
            boolean submitted = getMinecraftBukkitSnapshotClientHandler().submitSnapshot(snapshot);
            sender.sendMessage("Tavall Resource Game server surface players=" + snapshot.onlinePlayerCount() + " submitted=" + submitted);
            if (sender instanceof Player) {
                getMinecraftBukkitVisualHandler().renderSnapshotSubmitted((Player) sender, submitted);
            }
        } catch (IOException exception) {
            getMinecraftBukkitLogger().warning("Failed to submit Tavall Resource Game server snapshot: " + exception.getMessage());
            sender.sendMessage("Tavall Resource Game server snapshot failed: " + exception.getMessage());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!isKingdomCommand(command.getName()) && !isKingdomCommand(alias)) {
            return List.of();
        }
        return getKingdomCommandRouter().onTabComplete(sender, command, alias, args);
    }

    private boolean submitKingdomCommand(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("tavall.resourcegame.command")) {
            sender.sendMessage("Missing permission tavall.resourcegame.command.");
            return true;
        }
        Map<String, String> metadata = new LinkedHashMap<String, String>();
        metadata.put("serverId", getMinecraftBukkitServerConfig().serverId());
        metadata.put("surfaceIdentity", "BUKKIT_SERVER");
        metadata.put("proxyId", getMinecraftBukkitServerConfig().proxyId());
        metadata.put("alias", normalizeKingdomAlias(label));
        metadata.put("sourceType", sender instanceof Player ? "player" : "console");
        if (sender instanceof Player) {
            metadata.put("worldName", ((Player) sender).getWorld().getName());
        }

        try {
            FrontendCommandVerificationResult result = getMinecraftBukkitCommandClientHandler().submitCommand(
                    platformAccountId(sender),
                    sender.getName(),
                    rawKingdomInput(label, args),
                    "minecraft-bukkit-command-" + UUID.randomUUID(),
                    metadata
            );
            KingdomCommandSupport.renderFeedback(this, sender, rawKingdomInput(label, args), result);
            return true;
        } catch (IOException exception) {
            getMinecraftBukkitLogger().warning("Failed to submit Tavall Resource Game Bukkit kingdom command: " + exception.getMessage());
            sender.sendMessage(ChatColor.RED + "Command failed: " + exception.getMessage());
            return true;
        }
    }

    private boolean renderVisualCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Tavall Resource Game visual diagnostics require a player sender.");
            return true;
        }
        String visualType = args.length > 1 ? args[1].toUpperCase() : "CHAT";
        String body = args.length > 2 ? joinArgs(args, 2) : "Server visual path is online.";
        getMinecraftBukkitVisualHandler().renderVisualRequest((Player) sender, new MinecraftVisualRenderRequest(
                ResourceGameFrontendSurfaceIdentity.BUKKIT_SERVER,
                getMinecraftBukkitServerConfig().serverId(),
                ((Player) sender).getUniqueId().toString(),
                visualType,
                "Tavall Resource Game",
                body,
                Map.of("command", "tavallserver visual"),
                "minecraft-bukkit-visual-" + UUID.randomUUID()
        ));
        sender.sendMessage("Tavall Resource Game visual rendered type=" + visualType);
        return true;
    }

    private boolean submitInteractionCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Tavall Resource Game interaction diagnostics require a player sender.");
            return true;
        }
        Player player = (Player) sender;
        Map<String, String> arguments = new LinkedHashMap<String, String>();
        arguments.put("command", "tavallserver interact");
        arguments.put("worldName", player.getWorld().getName());
        arguments.put("message", args.length > 1 ? joinArgs(args, 1) : "diagnostic");

        Map<String, String> metadata = new LinkedHashMap<String, String>();
        metadata.put("serverId", getMinecraftBukkitServerConfig().serverId());
        metadata.put("surfaceIdentity", "BUKKIT_SERVER");
        metadata.put("proxyId", getMinecraftBukkitServerConfig().proxyId());

        try {
            InteractionResult result = getMinecraftBukkitCommandClientHandler().submitInteraction(new InteractionRequest(
                    "minecraft-bukkit-command-interact-" + UUID.randomUUID(),
                    player.getUniqueId().toString(),
                    InteractionTargetType.UNKNOWN,
                    "diagnostic",
                    "open_menu",
                    getMinecraftBukkitServerConfig().serverId(),
                    player.getWorld().getName(),
                    arguments,
                    System.currentTimeMillis()
            ));
            sender.sendMessage("Tavall Resource Game server interaction submitted=" + result.success() + " " + result.message());
            getMinecraftBukkitVisualHandler().renderSnapshotSubmitted(player, result.success());
        } catch (IOException exception) {
            getMinecraftBukkitLogger().warning("Failed to submit Tavall Resource Game server interaction: " + exception.getMessage());
            sender.sendMessage("Tavall Resource Game server interaction failed: " + exception.getMessage());
        }
        return true;
    }

    private String joinArgs(String[] args, int startIndex) {
        StringBuilder builder = new StringBuilder();
        for (int index = startIndex; index < args.length; index++) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(args[index]);
        }
        return builder.toString();
    }

    private boolean isKingdomCommand(String value) {
        return "kd".equalsIgnoreCase(value) || "kingdom".equalsIgnoreCase(value);
    }

    private List<String> matching(List<String> candidates, String prefix) {
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

    private String normalizeKingdomAlias(String label) {
        return "kingdom".equalsIgnoreCase(label) ? "kingdom" : "kd";
    }

    private String rawKingdomInput(String label, String[] args) {
        StringBuilder builder = new StringBuilder(normalizeKingdomAlias(label));
        for (String arg : args) {
            builder.append(' ').append(arg);
        }
        return builder.toString();
    }

    private String platformAccountId(CommandSender sender) {
        if (sender instanceof Player) {
            return ((Player) sender).getUniqueId().toString();
        }
        return "minecraft-console:" + getMinecraftBukkitServerConfig().serverId();
    }

}
