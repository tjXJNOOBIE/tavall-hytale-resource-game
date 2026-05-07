package com.tavall.hytale.resourcegame.middleware.control;

import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;
import com.tavall.hytale.resourcegame.middleware.common.MetadataMaps;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record ControlPlatformFanoutRetryRecord(
        UUID retryId,
        ControlCommandId commandId,
        ControlCommandType commandType,
        GamePlatform platform,
        List<String> changedObjectIds,
        int attemptCount,
        int maxAttempts,
        Instant nextAttemptAt,
        Optional<Instant> lastAttemptAt,
        ControlPlatformFanoutRetryState state,
        String message,
        Instant createdAt,
        Instant updatedAt,
        Map<String, String> metadata
) {
    public ControlPlatformFanoutRetryRecord {
        Objects.requireNonNull(retryId, "retryId");
        Objects.requireNonNull(commandId, "commandId");
        Objects.requireNonNull(commandType, "commandType");
        Objects.requireNonNull(platform, "platform");
        changedObjectIds = changedObjectIds == null ? List.of() : List.copyOf(changedObjectIds);
        attemptCount = Math.max(0, attemptCount);
        maxAttempts = Math.max(1, maxAttempts);
        Objects.requireNonNull(nextAttemptAt, "nextAttemptAt");
        lastAttemptAt = lastAttemptAt == null ? Optional.empty() : lastAttemptAt;
        state = state == null ? ControlPlatformFanoutRetryState.PENDING : state;
        message = message == null ? "" : message;
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");
        metadata = MetadataMaps.immutable(metadata);
    }

    public ControlPlatformFanoutRetryRecord withAttemptScheduled(Instant nextAttemptAt, Instant now, String message) {
        return new ControlPlatformFanoutRetryRecord(
                retryId,
                commandId,
                commandType,
                platform,
                changedObjectIds,
                attemptCount + 1,
                maxAttempts,
                nextAttemptAt,
                Optional.of(now),
                attemptCount + 1 >= maxAttempts ? ControlPlatformFanoutRetryState.FAILED : ControlPlatformFanoutRetryState.RETRYING,
                message,
                createdAt,
                now,
                metadata
        );
    }

    public ControlPlatformFanoutRetryRecord withState(ControlPlatformFanoutRetryState state, Instant now, String message) {
        return new ControlPlatformFanoutRetryRecord(
                retryId,
                commandId,
                commandType,
                platform,
                changedObjectIds,
                attemptCount,
                maxAttempts,
                nextAttemptAt,
                lastAttemptAt,
                state,
                message,
                createdAt,
                now,
                metadata
        );
    }
}
