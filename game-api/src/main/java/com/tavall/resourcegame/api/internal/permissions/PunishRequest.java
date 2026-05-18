package org.tavall.api.minecraft.permissions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendPlatform;

import java.util.Map;
import java.util.Objects;

public record PunishRequest(
        @JsonProperty("requestId")
        String requestId,
        @JsonProperty("operation")
        PunishOperationType operation,
        @JsonProperty("platform")
        ResourceGameFrontendPlatform platform,
        @JsonProperty("actorPlatformAccountId")
        String actorPlatformAccountId,
        @JsonProperty("actorDisplayName")
        String actorDisplayName,
        @JsonProperty("targetPlatformAccountId")
        String targetPlatformAccountId,
        @JsonProperty("targetDisplayName")
        String targetDisplayName,
        @JsonProperty("durationText")
        String durationText,
        @JsonProperty("reason")
        String reason,
        @JsonProperty("context")
        Map<String, String> context,
        @JsonProperty("createdAtEpochMillis")
        long createdAtEpochMillis
) {
    @JsonCreator
    public PunishRequest {
        Objects.requireNonNull(requestId, "requestId");
        Objects.requireNonNull(operation, "operation");
        Objects.requireNonNull(platform, "platform");
        Objects.requireNonNull(actorPlatformAccountId, "actorPlatformAccountId");
        Objects.requireNonNull(actorDisplayName, "actorDisplayName");
        Objects.requireNonNull(targetPlatformAccountId, "targetPlatformAccountId");
        Objects.requireNonNull(targetDisplayName, "targetDisplayName");
        if (requestId.isBlank()) {
            throw new IllegalArgumentException("requestId must not be blank");
        }
        if (actorPlatformAccountId.isBlank()) {
            throw new IllegalArgumentException("actorPlatformAccountId must not be blank");
        }
        if (actorDisplayName.isBlank()) {
            throw new IllegalArgumentException("actorDisplayName must not be blank");
        }
        if (targetPlatformAccountId.isBlank()) {
            throw new IllegalArgumentException("targetPlatformAccountId must not be blank");
        }
        if (targetDisplayName.isBlank()) {
            throw new IllegalArgumentException("targetDisplayName must not be blank");
        }
        durationText = durationText == null ? "" : durationText;
        reason = reason == null ? "" : reason;
        context = context == null ? Map.of() : Map.copyOf(context);
    }

    public static PunishRequest inspect(
            String requestId,
            ResourceGameFrontendPlatform platform,
            String actorPlatformAccountId,
            String actorDisplayName,
            String targetPlatformAccountId,
            String targetDisplayName,
            Map<String, String> context,
            long createdAtEpochMillis
    ) {
        return new PunishRequest(requestId, PunishOperationType.INSPECT, platform, actorPlatformAccountId, actorDisplayName, targetPlatformAccountId, targetDisplayName, "", "", context, createdAtEpochMillis);
    }

    public static PunishRequest ban(
            String requestId,
            ResourceGameFrontendPlatform platform,
            String actorPlatformAccountId,
            String actorDisplayName,
            String targetPlatformAccountId,
            String targetDisplayName,
            String durationText,
            String reason,
            Map<String, String> context,
            long createdAtEpochMillis
    ) {
        return new PunishRequest(requestId, PunishOperationType.BAN, platform, actorPlatformAccountId, actorDisplayName, targetPlatformAccountId, targetDisplayName, durationText, reason, context, createdAtEpochMillis);
    }

    public static PunishRequest warn(
            String requestId,
            ResourceGameFrontendPlatform platform,
            String actorPlatformAccountId,
            String actorDisplayName,
            String targetPlatformAccountId,
            String targetDisplayName,
            String reason,
            Map<String, String> context,
            long createdAtEpochMillis
    ) {
        return new PunishRequest(requestId, PunishOperationType.WARN, platform, actorPlatformAccountId, actorDisplayName, targetPlatformAccountId, targetDisplayName, "", reason, context, createdAtEpochMillis);
    }

    public static PunishRequest unban(
            String requestId,
            ResourceGameFrontendPlatform platform,
            String actorPlatformAccountId,
            String actorDisplayName,
            String targetPlatformAccountId,
            String targetDisplayName,
            Map<String, String> context,
            long createdAtEpochMillis
    ) {
        return new PunishRequest(requestId, PunishOperationType.UNBAN, platform, actorPlatformAccountId, actorDisplayName, targetPlatformAccountId, targetDisplayName, "", "", context, createdAtEpochMillis);
    }

    public static PunishRequest kick(
            String requestId,
            ResourceGameFrontendPlatform platform,
            String actorPlatformAccountId,
            String actorDisplayName,
            String targetPlatformAccountId,
            String targetDisplayName,
            String reason,
            Map<String, String> context,
            long createdAtEpochMillis
    ) {
        return new PunishRequest(requestId, PunishOperationType.KICK, platform, actorPlatformAccountId, actorDisplayName, targetPlatformAccountId, targetDisplayName, "", reason, context, createdAtEpochMillis);
    }

    public static PunishRequest mute(
            String requestId,
            ResourceGameFrontendPlatform platform,
            String actorPlatformAccountId,
            String actorDisplayName,
            String targetPlatformAccountId,
            String targetDisplayName,
            String durationText,
            String reason,
            Map<String, String> context,
            long createdAtEpochMillis
    ) {
        return new PunishRequest(requestId, PunishOperationType.MUTE, platform, actorPlatformAccountId, actorDisplayName, targetPlatformAccountId, targetDisplayName, durationText, reason, context, createdAtEpochMillis);
    }

    public static PunishRequest unmute(
            String requestId,
            ResourceGameFrontendPlatform platform,
            String actorPlatformAccountId,
            String actorDisplayName,
            String targetPlatformAccountId,
            String targetDisplayName,
            Map<String, String> context,
            long createdAtEpochMillis
    ) {
        return new PunishRequest(requestId, PunishOperationType.UNMUTE, platform, actorPlatformAccountId, actorDisplayName, targetPlatformAccountId, targetDisplayName, "", "", context, createdAtEpochMillis);
    }

    public static PunishRequest unwarn(
            String requestId,
            ResourceGameFrontendPlatform platform,
            String actorPlatformAccountId,
            String actorDisplayName,
            String targetPlatformAccountId,
            String targetDisplayName,
            Map<String, String> context,
            long createdAtEpochMillis
    ) {
        return new PunishRequest(requestId, PunishOperationType.UNWARN, platform, actorPlatformAccountId, actorDisplayName, targetPlatformAccountId, targetDisplayName, "", "", context, createdAtEpochMillis);
    }
}
