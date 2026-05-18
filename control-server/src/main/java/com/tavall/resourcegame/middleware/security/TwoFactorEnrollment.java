package org.tavall.control.security;

import org.tavall.control.common.MetadataMaps;
import org.tavall.control.identity.UniversalPlayerId;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record TwoFactorEnrollment(
        UUID enrollmentId,
        UniversalPlayerId universalPlayerId,
        TwoFactorMethod method,
        String secretEncryptedOrProtected,
        boolean enabled,
        Instant createdAt,
        Optional<Instant> verifiedAt,
        Optional<Instant> lastUsedAt,
        Map<String, String> metadata
) {
    public TwoFactorEnrollment {
        Objects.requireNonNull(enrollmentId, "enrollmentId");
        Objects.requireNonNull(universalPlayerId, "universalPlayerId");
        Objects.requireNonNull(method, "method");
        if (secretEncryptedOrProtected == null || secretEncryptedOrProtected.isBlank()) {
            throw new IllegalArgumentException("secretEncryptedOrProtected is required.");
        }
        Objects.requireNonNull(createdAt, "createdAt");
        verifiedAt = verifiedAt == null ? Optional.empty() : verifiedAt;
        lastUsedAt = lastUsedAt == null ? Optional.empty() : lastUsedAt;
        metadata = MetadataMaps.immutable(metadata);
    }

    public TwoFactorEnrollment enabled(Instant now) {
        return new TwoFactorEnrollment(enrollmentId, universalPlayerId, method, secretEncryptedOrProtected, true, createdAt, Optional.of(now), Optional.of(now), metadata);
    }

    public TwoFactorEnrollment used(Instant now) {
        return new TwoFactorEnrollment(enrollmentId, universalPlayerId, method, secretEncryptedOrProtected, enabled, createdAt, verifiedAt, Optional.of(now), metadata);
    }
}
