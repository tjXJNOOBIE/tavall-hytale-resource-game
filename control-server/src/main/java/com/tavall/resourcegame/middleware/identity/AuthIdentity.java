package org.tavall.control.identity;

import org.tavall.control.common.MetadataMaps;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record AuthIdentity(
        UUID authIdentityId,
        UniversalPlayerId universalPlayerId,
        AuthProvider provider,
        String providerSubject,
        Optional<String> email,
        boolean emailVerified,
        Instant createdAt,
        Instant lastUsedAt,
        Map<String, String> metadata
) {
    public AuthIdentity {
        Objects.requireNonNull(authIdentityId, "authIdentityId");
        Objects.requireNonNull(universalPlayerId, "universalPlayerId");
        Objects.requireNonNull(provider, "provider");
        if (providerSubject == null || providerSubject.isBlank()) {
            throw new IllegalArgumentException("providerSubject is required.");
        }
        email = email == null ? Optional.empty() : email.map(String::toLowerCase);
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(lastUsedAt, "lastUsedAt");
        metadata = MetadataMaps.immutable(metadata);
    }
}
