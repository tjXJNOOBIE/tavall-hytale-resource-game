package com.tavall.resourcegame.middleware.security;

import com.tavall.resourcegame.middleware.common.HighRiskAction;
import com.tavall.resourcegame.middleware.identity.UniversalPlayerId;

public final class HighRiskActionChallengeHandler implements ISecurityDomain {
    public HighRiskActionChallengeHandler() {
    }

    public HighRiskActionChallengeHandler(TwoFactorRepository twoFactorRepository) {
        registerTwoFactorRepository(twoFactorRepository);
    }

    public boolean requiresTwoFactorChallenge(UniversalPlayerId universalPlayerId, HighRiskAction action) {
        return getTwoFactorRepository().findEnabledEnrollment(universalPlayerId).isPresent() && action != null;
    }

    public void validateHighRiskAction(UniversalPlayerId universalPlayerId, HighRiskAction action, boolean challengeVerified) {
        if (requiresTwoFactorChallenge(universalPlayerId, action) && !challengeVerified) {
            throw new SecurityException("Two-factor verification is required for high-risk action " + action + ".");
        }
    }
}
