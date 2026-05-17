package com.tavall.resourcegame.frontend.minecraft.commands;

import com.tavall.resourcegame.api.internal.frontend.ResourceGameFrontendPlatform;
import com.tavall.resourcegame.api.internal.permissions.RankRequest;
import com.tavall.resourcegame.api.internal.permissions.RankResponse;
import com.tavall.resourcegame.api.internal.permissions.UniversalPermissionRole;
import com.tavall.resourcegame.api.internal.permissions.UniversalPermissionSubject;
import com.tavall.resourcegame.frontend.minecraft.routing.MinecraftVelocityCommandResult;
import com.tavall.resourcegame.frontend.minecraft.commands.source.MinecraftVelocityCommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

public final class MinecraftVelocityRankCommand extends MinecraftVelocityProxyCommandSupport implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        if (invocation.source() instanceof ConsoleCommandSource) {
            invocation.source().sendMessage(Component.text("The rank command is player-only in this runtime."));
            return;
        }
        if (!(invocation.source() instanceof Player)) {
            invocation.source().sendMessage(Component.text("Only players can use /rank in this runtime."));
            return;
        }
        MinecraftVelocityCommandResult result = execute(commandSource(invocation), invocation.alias(), invocation.arguments());
        invocation.source().sendMessage(Component.text(result.message()));
    }

    public MinecraftVelocityCommandResult execute(MinecraftVelocityCommandSource source, String alias, String[] args) {
        if (!canUse(source) && !canManage(source) && !source.sourceType().equals("console")) {
            return MinecraftVelocityCommandResult.denied("Missing permission " + getMinecraftProxyConfig().commandPermission() + ".");
        }
        if (args.length == 0) {
            return new MinecraftVelocityCommandResult(true, helpMessage(source));
        }
        return switch (normalize(args[0])) {
            case "list", "all" -> list(source, alias);
            case "inspect", "get", "show", "view" -> inspect(source, alias, args);
            case "set", "update", "grant" -> set(source, alias, args);
            case "remove", "revoke" -> remove(source, alias, args);
            default -> new MinecraftVelocityCommandResult(true, helpMessage(source));
        };
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }

    private MinecraftVelocityCommandResult list(MinecraftVelocityCommandSource source, String alias) {
        if (!canManage(source)) {
            return MinecraftVelocityCommandResult.denied("Missing permission " + getMinecraftProxyConfig().adminPermission() + ".");
        }
        RankResponse response = getMinecraftControlCommandClient().submitRankRequest(RankRequest.list(
                requestId(),
                ResourceGameFrontendPlatform.MINECRAFT,
                source.platformAccountId(),
                source.platformDisplayName(),
                baseContext(source, alias),
                Instant.now().toEpochMilli()
        ));
        return new MinecraftVelocityCommandResult(response.success(), response.message());
    }

    private MinecraftVelocityCommandResult inspect(MinecraftVelocityCommandSource source, String alias, String[] args) {
        if (args.length < 2) {
            return MinecraftVelocityCommandResult.denied("Usage: /rank inspect <name>");
        }
        String targetName = args[1];
        RankResponse response = getMinecraftControlCommandClient().submitRankRequest(RankRequest.inspect(
                requestId(),
                ResourceGameFrontendPlatform.MINECRAFT,
                source.platformAccountId(),
                source.platformDisplayName(),
                resolveTargetAccountId(targetName),
                resolveTargetDisplayName(targetName),
                baseContext(source, alias),
                Instant.now().toEpochMilli()
        ));
        if (!response.success() || response.subject() == null) {
            return new MinecraftVelocityCommandResult(response.success(), response.message());
        }
        return new MinecraftVelocityCommandResult(true, renderInspectMessage(response.subject()));
    }

    private MinecraftVelocityCommandResult set(MinecraftVelocityCommandSource source, String alias, String[] args) {
        if (!canManage(source)) {
            return MinecraftVelocityCommandResult.denied("Missing permission " + getMinecraftProxyConfig().adminPermission() + ".");
        }
        if (args.length != 3) {
            return MinecraftVelocityCommandResult.denied("Usage: /rank set <name> <rank_name>");
        }
        UniversalPermissionRole requestedRole = resolveRole(args[2]);
        if (requestedRole == null) {
            return MinecraftVelocityCommandResult.denied("Rank does not exist!");
        }
        String targetName = args[1];
        RankResponse response = getMinecraftControlCommandClient().submitRankRequest(RankRequest.setRole(
                requestId(),
                ResourceGameFrontendPlatform.MINECRAFT,
                source.platformAccountId(),
                source.platformDisplayName(),
                resolveTargetAccountId(targetName),
                resolveTargetDisplayName(targetName),
                requestedRole,
                baseContext(source, alias),
                Instant.now().toEpochMilli()
        ));
        return new MinecraftVelocityCommandResult(response.success(), response.message());
    }

    private MinecraftVelocityCommandResult remove(MinecraftVelocityCommandSource source, String alias, String[] args) {
        if (!canManage(source)) {
            return MinecraftVelocityCommandResult.denied("Missing permission " + getMinecraftProxyConfig().adminPermission() + ".");
        }
        if (args.length < 2 || args.length > 3) {
            return MinecraftVelocityCommandResult.denied("Usage: /rank remove <name> [fallback_rank]");
        }
        UniversalPermissionRole fallbackRole = args.length == 3 ? resolveRole(args[2]) : UniversalPermissionRole.MEMBER;
        if (fallbackRole == null) {
            return MinecraftVelocityCommandResult.denied("Rank does not exist!");
        }
        String targetName = args[1];
        RankResponse response = getMinecraftControlCommandClient().submitRankRequest(RankRequest.setRole(
                requestId(),
                ResourceGameFrontendPlatform.MINECRAFT,
                source.platformAccountId(),
                source.platformDisplayName(),
                resolveTargetAccountId(targetName),
                resolveTargetDisplayName(targetName),
                fallbackRole,
                baseContext(source, alias),
                Instant.now().toEpochMilli()
        ));
        return new MinecraftVelocityCommandResult(response.success(), response.message());
    }

    private String helpMessage(MinecraftVelocityCommandSource source) {
        StringBuilder builder = new StringBuilder("Rank commands:\n")
                .append("/rank inspect <target>  View a player's current rank");
        if (canManage(source)) {
            builder.append("\n/rank list  List known rank subjects")
                    .append("\n/rank set <target> <role>  Update a player's rank")
                    .append("\n/rank remove <target> [fallback_role]  Reset a player's rank");
        }
        return builder.toString();
    }

    private String renderInspectMessage(UniversalPermissionSubject subject) {
        java.util.Set<String> permissions = new java.util.TreeSet<>();
        subject.role().permissions().forEach(permission -> permissions.add(permission.name()));
        subject.explicitPermissions().forEach(permission -> permissions.add(permission.name()));
        return "Loaded rank subject for "
                + subject.displayName()
                + ". Rank: "
                + subject.role().name()
                + " (power "
                + subject.role().powerLevel()
                + ")\nPermissions: "
                + (permissions.isEmpty() ? "none" : String.join(", ", permissions));
    }

    private UniversalPermissionRole resolveRole(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        return switch (normalize(input)) {
            case "member", "default", "user" -> UniversalPermissionRole.MEMBER;
            case "mod", "moderator" -> UniversalPermissionRole.MODERATOR;
            case "admin" -> UniversalPermissionRole.ADMIN;
            case "owner" -> UniversalPermissionRole.OWNER;
            case "system" -> UniversalPermissionRole.SYSTEM;
            default -> null;
        };
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String requestId() {
        return "minecraft-rank-" + UUID.randomUUID();
    }
}
