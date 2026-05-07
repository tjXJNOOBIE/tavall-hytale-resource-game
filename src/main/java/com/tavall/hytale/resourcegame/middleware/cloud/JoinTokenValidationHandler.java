package com.tavall.hytale.resourcegame.middleware.cloud;

import java.time.Instant;
import java.util.Optional;

public final class JoinTokenValidationHandler {
    private final InMemoryCloudRepository repository;
    private final CloudSecretHasher hasher;

    public JoinTokenValidationHandler(InMemoryCloudRepository repository, CloudSecretHasher hasher) {
        this.repository = repository;
        this.hasher = hasher;
    }

    public Optional<JoinToken> validate(String plaintextToken, Instant now) {
        if (plaintextToken == null || plaintextToken.isBlank()) {
            return Optional.empty();
        }
        return repository.findJoinTokenByHash(hasher.sha256(plaintextToken))
                .filter(token -> token.activeAt(now));
    }
}
