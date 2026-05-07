package com.tavall.hytale.resourcegame.middleware.control;

import com.tavall.hytale.resourcegame.middleware.common.MetadataMaps;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record ControlOperator(
        UUID operatorId,
        Optional<UniversalPlayerId> universalPlayerId,
        String displayName,
        ControlOperatorRole role,
        boolean enabled,
        Instant createdAt,
        Map<String, String> metadata
) {
    public ControlOperator {
        Objects.requireNonNull(operatorId, "operatorId");
        universalPlayerId = universalPlayerId == null ? Optional.empty() : universalPlayerId;
        displayName = displayName == null || displayName.isBlank() ? "control-operator" : displayName;
        role = role == null ? ControlOperatorRole.VIEWER : role;
        Objects.requireNonNull(createdAt, "createdAt");
        metadata = MetadataMaps.immutable(metadata);
    }

    public static ControlOperator system(Instant now) {
        return new ControlOperator(UUID.nameUUIDFromBytes("system-control-operator".getBytes()), Optional.empty(), "SYSTEM", ControlOperatorRole.SYSTEM, true, now, Map.of());
    }

    public static ControlOperator localOwner(Instant now) {
        return new ControlOperator(UUID.nameUUIDFromBytes("local-owner-control-operator".getBytes()), Optional.empty(), "Local Owner", ControlOperatorRole.OWNER, true, now, Map.of("localRuntime", "true"));
    }
}
