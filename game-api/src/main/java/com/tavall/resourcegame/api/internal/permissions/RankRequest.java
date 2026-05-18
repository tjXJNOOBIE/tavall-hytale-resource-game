package org.tavall.api.minecraft.permissions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendPlatform;
import org.tavall.api.minecraft.permissions.UniversalPermissionRole;

import java.util.Map;
import java.util.Objects;

public record RankRequest(
        @JsonProperty("requestId")
        String requestId,
        @JsonProperty("operation")
        RankOperationType operation,
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
        @JsonProperty("requestedRole")
        UniversalPermissionRole requestedRole,
        @JsonProperty("context")
        Map<String, String> context,
        @JsonProperty("createdAtEpochMillis")
        long createdAtEpochMillis
) {
    @JsonCreator
    public RankRequest {
        Objects.requireNonNull(requestId, "requestId");
        Objects.requireNonNull(operation, "operation");
        Objects.requireNonNull(platform, "platform");
        Objects.requireNonNull(actorPlatformAccountId, "actorPlatformAccountId");
        Objects.requireNonNull(actorDisplayName, "actorDisplayName");
        if (requestId.isBlank()) {
            throw new IllegalArgumentException("requestId must not be blank");
        }
        if (actorPlatformAccountId.isBlank()) {
            throw new IllegalArgumentException("actorPlatformAccountId must not be blank");
        }
        if (actorDisplayName.isBlank()) {
            throw new IllegalArgumentException("actorDisplayName must not be blank");
        }
        context = context == null ? Map.of() : Map.copyOf(context);
    }

    public static RankRequest list(
            String requestId,
            ResourceGameFrontendPlatform platform,
            String actorPlatformAccountId,
            String actorDisplayName,
            Map<String, String> context,
            long createdAtEpochMillis
    ) {
        return new RankRequest(
                requestId,
                RankOperationType.LIST,
                platform,
                actorPlatformAccountId,
                actorDisplayName,
                null,
                null,
                null,
                context,
                createdAtEpochMillis
        );
    }

    public static RankRequest inspect(
            String requestId,
            ResourceGameFrontendPlatform platform,
            String actorPlatformAccountId,
            String actorDisplayName,
            String targetPlatformAccountId,
            String targetDisplayName,
            Map<String, String> context,
            long createdAtEpochMillis
    ) {
        return new RankRequest(
                requestId,
                RankOperationType.INSPECT,
                platform,
                actorPlatformAccountId,
                actorDisplayName,
                targetPlatformAccountId,
                targetDisplayName,
                null,
                context,
                createdAtEpochMillis
        );
    }

    public static RankRequest setRole(
            String requestId,
            ResourceGameFrontendPlatform platform,
            String actorPlatformAccountId,
            String actorDisplayName,
            String targetPlatformAccountId,
            String targetDisplayName,
            UniversalPermissionRole requestedRole,
            Map<String, String> context,
            long createdAtEpochMillis
    ) {
        return new RankRequest(
                requestId,
                RankOperationType.SET_ROLE,
                platform,
                actorPlatformAccountId,
                actorDisplayName,
                targetPlatformAccountId,
                targetDisplayName,
                requestedRole,
                context,
                createdAtEpochMillis
        );
    }
}
