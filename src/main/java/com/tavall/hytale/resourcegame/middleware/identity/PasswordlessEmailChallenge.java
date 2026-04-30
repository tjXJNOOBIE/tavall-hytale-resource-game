package com.tavall.hytale.resourcegame.middleware.identity;

import com.tavall.hytale.resourcegame.middleware.common.MetadataMaps;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record PasswordlessEmailChallenge(
        UUID challengeId,
        String email,
        String challengeTokenHash,
        Instant expiresAt,
        Optional<Instant> consumedAt,
        int attemptCount,
        Map<String, String> metadata
) {
    public PasswordlessEmailChallenge {
        Objects.requireNonNull(challengeId, "challengeId");
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email is required.");
        }
        email = email.toLowerCase();
        if (challengeTokenHash == null || challengeTokenHash.isBlank()) {
            throw new IllegalArgumentException("challengeTokenHash is required.");
        }
        Objects.requireNonNull(expiresAt, "expiresAt");
        consumedAt = consumedAt == null ? Optional.empty() : consumedAt;
        attemptCount = Math.max(0, attemptCount);
        metadata = MetadataMaps.immutable(metadata);
    }

    public PasswordlessEmailChallenge withAttemptCount(int attemptCount) {
        return new PasswordlessEmailChallenge(challengeId, email, challengeTokenHash, expiresAt, consumedAt, attemptCount, metadata);
    }

    public PasswordlessEmailChallenge consumed(Instant now) {
        return new PasswordlessEmailChallenge(challengeId, email, challengeTokenHash, expiresAt, Optional.of(now), attemptCount, metadata);
    }
}
