package com.tavall.resourcegame.frontend.minecraft;

import com.tavall.resourcegame.shared.frontend.PunishRequest;
import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendPlatform;
import com.tavall.resourcegame.shared.permissions.UniversalPermission;
import com.tavall.resourcegame.shared.permissions.UniversalPermissionPolicy;
import com.tavall.resourcegame.shared.permissions.UniversalPermissionSubject;
import com.tavall.resourcegame.dependency.DependencyLoaderAccess;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

abstract class MinecraftVelocityPunishmentCommandSupport implements IMinecraftFrontendDomain, IDependencyInjectableConcrete {
    protected MinecraftVelocityCommandSource commandSource(SimpleCommand.Invocation invocation) {
        if (invocation.source() instanceof Player player) {
            return new PlayerVelocityCommandSource(player);
        }
        if (invocation.source() instanceof ConsoleCommandSource console) {
            return new ConsoleVelocityCommandSource(console);
        }
        return new GenericVelocityCommandSource(invocation.source());
    }

    protected boolean canManage(MinecraftVelocityCommandSource source) {
        UniversalPermissionSubject subject = getMinecraftVelocityPermissionResolver().resolveSubject(source);
        return new UniversalPermissionPolicy().canExecuteAdminCommand(subject);
    }

    protected Map<String, String> baseContext(MinecraftVelocityCommandSource source, String alias) {
        Map<String, String> context = new LinkedHashMap<>(source.metadata());
        context.put("sourceType", source.sourceType());
        context.put("proxy", getMinecraftProxyConfig().serverId());
        context.put("alias", alias);
        context.put("surfaceIdentity", "VELOCITY_PROXY");
        return context;
    }

    protected String resolveTargetAccountId(String targetName) {
        Optional<Player> player = resolveProxyServer().flatMap(proxyServer -> proxyServer.getPlayer(targetName));
        if (player.isPresent()) {
            return player.get().getUniqueId().toString();
        }
        return targetName;
    }

    protected String resolveTargetDisplayName(String targetName) {
        Optional<Player> player = resolveProxyServer().flatMap(proxyServer -> proxyServer.getPlayer(targetName));
        return player.map(Player::getUsername).orElse(targetName);
    }

    protected Optional<Player> resolveOnlineTarget(String targetName) {
        return resolveProxyServer().flatMap(proxyServer -> proxyServer.getPlayer(targetName));
    }

    protected PunishRequest inspectRequest(MinecraftVelocityCommandSource source, String alias, String targetName) {
        String targetAccountId = resolveTargetAccountId(targetName);
        String targetDisplayName = resolveTargetDisplayName(targetName);
        return PunishRequest.inspect(
                "minecraft-punish-" + UUID.randomUUID(),
                ResourceGameFrontendPlatform.MINECRAFT,
                source.platformAccountId(),
                source.platformDisplayName(),
                targetAccountId,
                targetDisplayName,
                baseContext(source, alias),
                Instant.now().toEpochMilli()
        );
    }

    protected PunishRequest banRequest(MinecraftVelocityCommandSource source, String alias, String targetName, String durationText, String reason) {
        String targetAccountId = resolveTargetAccountId(targetName);
        String targetDisplayName = resolveTargetDisplayName(targetName);
        return PunishRequest.ban(
                "minecraft-punish-" + UUID.randomUUID(),
                ResourceGameFrontendPlatform.MINECRAFT,
                source.platformAccountId(),
                source.platformDisplayName(),
                targetAccountId,
                targetDisplayName,
                durationText,
                reason,
                baseContext(source, alias),
                Instant.now().toEpochMilli()
        );
    }

    protected PunishRequest warnRequest(MinecraftVelocityCommandSource source, String alias, String targetName, String reason) {
        String targetAccountId = resolveTargetAccountId(targetName);
        String targetDisplayName = resolveTargetDisplayName(targetName);
        return PunishRequest.warn(
                "minecraft-punish-" + UUID.randomUUID(),
                ResourceGameFrontendPlatform.MINECRAFT,
                source.platformAccountId(),
                source.platformDisplayName(),
                targetAccountId,
                targetDisplayName,
                reason,
                baseContext(source, alias),
                Instant.now().toEpochMilli()
        );
    }

    protected PunishRequest unbanRequest(MinecraftVelocityCommandSource source, String alias, String targetName) {
        String targetAccountId = resolveTargetAccountId(targetName);
        String targetDisplayName = resolveTargetDisplayName(targetName);
        return PunishRequest.unban(
                "minecraft-punish-" + UUID.randomUUID(),
                ResourceGameFrontendPlatform.MINECRAFT,
                source.platformAccountId(),
                source.platformDisplayName(),
                targetAccountId,
                targetDisplayName,
                baseContext(source, alias),
                Instant.now().toEpochMilli()
        );
    }

    protected Duration determineBanDuration(String durationText) {
        if (durationText == null || durationText.isBlank() || durationText.equalsIgnoreCase("permanent") || durationText.equalsIgnoreCase("p")) {
            return Duration.ZERO;
        }
        try {
            return parseDuration(durationText.trim());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    protected Duration parseDuration(String durationText) {
        String normalized = durationText == null ? "" : durationText.trim().toLowerCase(java.util.Locale.ROOT);
        if (normalized.isBlank() || normalized.equals("p") || normalized.equals("permanent")) {
            return Duration.ZERO;
        }
        long amount;
        long multiplier;
        if (normalized.endsWith("mo")) {
            amount = parseLeadingLong(normalized.substring(0, normalized.length() - 2));
            multiplier = 30L * 24L * 60L * 60L;
        } else if (normalized.endsWith("yr")) {
            amount = parseLeadingLong(normalized.substring(0, normalized.length() - 2));
            multiplier = 365L * 24L * 60L * 60L;
        } else {
            if (normalized.length() < 2) {
                throw new IllegalArgumentException("Invalid duration");
            }
            amount = parseLeadingLong(normalized.substring(0, normalized.length() - 1));
            multiplier = switch (normalized.substring(normalized.length() - 1)) {
                case "m" -> 60L;
                case "h" -> 60L * 60L;
                case "d" -> 24L * 60L * 60L;
                case "w" -> 7L * 24L * 60L * 60L;
                default -> throw new IllegalArgumentException("Invalid duration");
            };
        }
        return Duration.ofSeconds(amount * multiplier);
    }

    private long parseLeadingLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid duration", exception);
        }
    }

    protected String safeMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message;
    }

    private Optional<IMinecraftVelocityProxyServer> resolveProxyServer() {
        return DependencyLoaderAccess.findOptionalInstance(IMinecraftVelocityProxyServer.class);
    }
}
