package com.tavall.resourcegame.middleware.cloud;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class JoinTokenCreationHandler implements IJoinTokenCreationHandler, ICloudControlDomain {
    private final SecureRandom random = new SecureRandom();

    public String createJoinToken(UUID createdBy, Instant expiresAt, Optional<String> region, Set<CloudNodeCapability> allowedCapabilities) {
        byte[] tokenBytes = new byte[32];
        random.nextBytes(tokenBytes);
        String plaintextToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        JoinToken joinToken = new JoinToken(
                UUID.randomUUID(),
                getCloudSecretHasher().sha256(plaintextToken),
                createdBy,
                expiresAt,
                Optional.empty(),
                Optional.empty(),
                region,
                allowedCapabilities,
                Map.of()
        );
        getCloudRepository().saveJoinToken(joinToken);
        return plaintextToken;
    }
}
