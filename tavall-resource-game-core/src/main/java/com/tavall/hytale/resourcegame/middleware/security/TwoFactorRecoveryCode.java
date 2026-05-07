package com.tavall.hytale.resourcegame.middleware.security;

import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record TwoFactorRecoveryCode(
        UUID recoveryCodeId,
        UniversalPlayerId universalPlayerId,
        String codeHash,
        Optional<Instant> usedAt,
        Instant createdAt
) {
    public TwoFactorRecoveryCode {
        Objects.requireNonNull(recoveryCodeId, "recoveryCodeId");
        Objects.requireNonNull(universalPlayerId, "universalPlayerId");
        if (codeHash == null || codeHash.isBlank()) {
            throw new IllegalArgumentException("codeHash is required.");
        }
        usedAt = usedAt == null ? Optional.empty() : usedAt;
        Objects.requireNonNull(createdAt, "createdAt");
    }

    public TwoFactorRecoveryCode used(Instant now) {
        return new TwoFactorRecoveryCode(recoveryCodeId, universalPlayerId, codeHash, Optional.of(now), createdAt);
    }
}
