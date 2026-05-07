package com.tavall.hytale.resourcegame.middleware.security;

import com.tavall.hytale.resourcegame.middleware.common.HighRiskAction;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;

public final class HighRiskActionChallengeHandler {
    private final TwoFactorRepository twoFactorRepository;

    public HighRiskActionChallengeHandler(TwoFactorRepository twoFactorRepository) {
        this.twoFactorRepository = twoFactorRepository;
    }

    public boolean requiresTwoFactorChallenge(UniversalPlayerId universalPlayerId, HighRiskAction action) {
        return twoFactorRepository.findEnabledEnrollment(universalPlayerId).isPresent() && action != null;
    }

    public void validateHighRiskAction(UniversalPlayerId universalPlayerId, HighRiskAction action, boolean challengeVerified) {
        if (requiresTwoFactorChallenge(universalPlayerId, action) && !challengeVerified) {
            throw new SecurityException("Two-factor verification is required for high-risk action " + action + ".");
        }
    }
}
