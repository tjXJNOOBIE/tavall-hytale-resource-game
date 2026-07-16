package org.tavall.api.minecraft.backend.rank;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record RankPlayerProfile(
        String platformAccountId,
        String displayName,
        String rankName,
        int powerLevel,
        Set<String> permissions,
        Map<String, String> metadata,
        Instant createdAt,
        Instant updatedAt
) {
    public RankPlayerProfile {
        Objects.requireNonNull(platformAccountId, "platformAccountId");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");
        if (platformAccountId.isBlank()) {
            throw new IllegalArgumentException("platformAccountId must not be blank");
        }
        displayName = displayName == null || displayName.isBlank() ? platformAccountId : displayName;
        rankName = Objects.requireNonNull(rankName, "rankName");
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public RankPlayerProfile withRank(RankDefinition definition, Instant updatedAt) {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(updatedAt, "updatedAt");
        return new RankPlayerProfile(
                platformAccountId,
                displayName,
                definition.rankName(),
                definition.powerLevel(),
                definition.permissions(),
                metadata,
                createdAt,
                updatedAt
        );
    }

    public RankPlayerProfile withDisplayName(String displayName, Instant updatedAt) {
        Objects.requireNonNull(updatedAt, "updatedAt");
        return new RankPlayerProfile(
                platformAccountId,
                displayName,
                rankName,
                powerLevel,
                permissions,
                metadata,
                createdAt,
                updatedAt
        );
    }

    public RankPlayerProfile withMetadata(Map<String, String> metadata, Instant updatedAt) {
        Objects.requireNonNull(updatedAt, "updatedAt");
        return new RankPlayerProfile(
                platformAccountId,
                displayName,
                rankName,
                powerLevel,
                permissions,
                metadata,
                createdAt,
                updatedAt
        );
    }
}
