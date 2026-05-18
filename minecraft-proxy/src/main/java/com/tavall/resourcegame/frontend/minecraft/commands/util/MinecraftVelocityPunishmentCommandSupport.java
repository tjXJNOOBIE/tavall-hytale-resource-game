package org.tavall.minecraft.commands.util;

import org.tavall.api.minecraft.frontend.ResourceGameFrontendPlatform;
import org.tavall.api.minecraft.permissions.PunishRequest;
import org.tavall.minecraft.commands.source.MinecraftVelocityCommandSource;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public abstract class MinecraftVelocityPunishmentCommandSupport extends MinecraftVelocityProxyCommandSupport {
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

    protected PunishRequest kickRequest(MinecraftVelocityCommandSource source, String alias, String targetName, String reason) {
        String targetAccountId = resolveTargetAccountId(targetName);
        String targetDisplayName = resolveTargetDisplayName(targetName);
        return PunishRequest.kick(
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

    protected PunishRequest muteRequest(MinecraftVelocityCommandSource source, String alias, String targetName, String durationText, String reason) {
        String targetAccountId = resolveTargetAccountId(targetName);
        String targetDisplayName = resolveTargetDisplayName(targetName);
        return PunishRequest.mute(
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

    protected PunishRequest unmuteRequest(MinecraftVelocityCommandSource source, String alias, String targetName) {
        String targetAccountId = resolveTargetAccountId(targetName);
        String targetDisplayName = resolveTargetDisplayName(targetName);
        return PunishRequest.unmute(
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

    protected PunishRequest unwarnRequest(MinecraftVelocityCommandSource source, String alias, String targetName) {
        String targetAccountId = resolveTargetAccountId(targetName);
        String targetDisplayName = resolveTargetDisplayName(targetName);
        return PunishRequest.unwarn(
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

    protected Duration determineTimedDuration(String durationText) {
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
}
