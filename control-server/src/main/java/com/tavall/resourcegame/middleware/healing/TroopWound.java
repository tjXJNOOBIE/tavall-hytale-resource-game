package org.tavall.control.healing;

import org.tavall.control.common.MetadataMaps;
import org.tavall.control.troop.TroopId;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record TroopWound(
        UUID woundId,
        TroopId troopId,
        WoundType woundType,
        WoundSeverity severity,
        Instant createdAt,
        Optional<Instant> healedAt,
        Map<String, String> metadata
) {
    public TroopWound {
        Objects.requireNonNull(woundId, "woundId");
        Objects.requireNonNull(troopId, "troopId");
        Objects.requireNonNull(woundType, "woundType");
        severity = severity == null ? WoundSeverity.MINOR : severity;
        Objects.requireNonNull(createdAt, "createdAt");
        healedAt = healedAt == null ? Optional.empty() : healedAt;
        metadata = MetadataMaps.immutable(metadata);
    }

    public static TroopWound active(TroopId troopId, WoundType woundType, WoundSeverity severity, Instant now) {
        return new TroopWound(UUID.randomUUID(), troopId, woundType, severity, now, Optional.empty(), Map.of());
    }

    public boolean active() {
        return healedAt.isEmpty();
    }

    public TroopWound markHealed(Instant healedAt) {
        return new TroopWound(woundId, troopId, woundType, severity, createdAt, Optional.of(healedAt), metadata);
    }
}
