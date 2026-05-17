package com.tavall.resourcegame.middleware.identity;

import java.util.Optional;
import java.util.UUID;

public interface PlatformLinkChallengeRepository {
    PlatformLinkChallenge savePlatformLinkChallenge(PlatformLinkChallenge challenge);

    Optional<PlatformLinkChallenge> findPlatformLinkChallenge(UUID challengeId);
}
