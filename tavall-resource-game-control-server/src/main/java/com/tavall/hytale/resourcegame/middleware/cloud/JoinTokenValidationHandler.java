package com.tavall.hytale.resourcegame.middleware.cloud;

import java.time.Instant;
import java.util.Optional;

public final class JoinTokenValidationHandler implements IJoinTokenValidationHandler, ICloudControlDomain {
    public Optional<JoinToken> validate(String plaintextToken, Instant now) {
        if (plaintextToken == null || plaintextToken.isBlank()) {
            return Optional.empty();
        }
        return getCloudRepository().findJoinTokenByHash(getCloudSecretHasher().sha256(plaintextToken))
                .filter(token -> token.activeAt(now));
    }
}
