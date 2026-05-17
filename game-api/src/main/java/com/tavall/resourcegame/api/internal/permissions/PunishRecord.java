package com.tavall.resourcegame.api.internal.permissions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

public record PunishRecord(
        @JsonProperty("targetPlatformAccountId")
        String targetPlatformAccountId,
        @JsonProperty("targetDisplayName")
        String targetDisplayName,
        @JsonProperty("operation")
        PunishOperationType operation,
        @JsonProperty("active")
        boolean active,
        @JsonProperty("reason")
        String reason,
        @JsonProperty("senderDisplayName")
        String senderDisplayName,
        @JsonProperty("durationText")
        String durationText,
        @JsonProperty("createdAtEpochMillis")
        long createdAtEpochMillis,
        @JsonProperty("expiresAtEpochMillis")
        Long expiresAtEpochMillis,
        @JsonProperty("metadata")
        Map<String, String> metadata
) {
    @JsonCreator
    public PunishRecord {
        Objects.requireNonNull(targetPlatformAccountId, "targetPlatformAccountId");
        Objects.requireNonNull(targetDisplayName, "targetDisplayName");
        Objects.requireNonNull(operation, "operation");
        Objects.requireNonNull(reason, "reason");
        Objects.requireNonNull(senderDisplayName, "senderDisplayName");
        Objects.requireNonNull(durationText, "durationText");
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
