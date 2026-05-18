package org.tavall.control.identity;

import java.util.Optional;
import java.util.UUID;

public interface PasswordlessEmailChallengeRepository {
    PasswordlessEmailChallenge savePasswordlessChallenge(PasswordlessEmailChallenge challenge);

    Optional<PasswordlessEmailChallenge> findPasswordlessChallenge(UUID challengeId);
}
