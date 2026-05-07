package com.tavall.hytale.resourcegame.middleware.identity;

import com.tavall.hytale.resourcegame.middleware.common.MetadataMaps;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;

public record UniversalPlayerAccount(
        UniversalPlayerId universalPlayerId,
        String displayName,
        Optional<String> primaryEmail,
        Instant createdAt,
        Instant updatedAt,
        boolean disabled,
        Map<String, String> metadata
) {
    public UniversalPlayerAccount {
        Objects.requireNonNull(universalPlayerId, "universalPlayerId");
        displayName = displayName == null || displayName.isBlank() ? "Player" : displayName;
        primaryEmail = primaryEmail == null ? Optional.empty() : primaryEmail.map(String::toLowerCase);
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");
        metadata = MetadataMaps.immutable(metadata);
    }

    public UniversalPlayerAccount withUpdatedAt(Instant updatedAt) {
        return new UniversalPlayerAccount(universalPlayerId, displayName, primaryEmail, createdAt, updatedAt, disabled, metadata);
    }
}
