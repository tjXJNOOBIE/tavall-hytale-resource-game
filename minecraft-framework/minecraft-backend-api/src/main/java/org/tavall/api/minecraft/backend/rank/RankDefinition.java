package org.tavall.api.minecraft.backend.rank;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public record RankDefinition(
        String rankName,
        int powerLevel,
        Set<String> permissions,
        Instant createdAt,
        Instant updatedAt
) {
    public RankDefinition {
        Objects.requireNonNull(rankName, "rankName");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
    }

    public RankDefinition withUpdatedAt(Instant updatedAt) {
        return new RankDefinition(rankName, powerLevel, permissions, createdAt, updatedAt);
    }
}
