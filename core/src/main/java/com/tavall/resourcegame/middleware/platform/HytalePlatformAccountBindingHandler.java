package com.tavall.resourcegame.middleware.platform;

import com.tavall.resourcegame.middleware.common.GamePlatform;
import com.tavall.resourcegame.middleware.identity.PlatformAccountBinding;
import com.tavall.resourcegame.middleware.identity.PlatformAccountLinkHandler;
import com.tavall.resourcegame.middleware.identity.PlatformLinkChallengeCreated;
import com.tavall.resourcegame.middleware.identity.UniversalPlayerId;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class HytalePlatformAccountBindingHandler implements IPlatformBindingDomain {
    public HytalePlatformAccountBindingHandler() {
    }

    public HytalePlatformAccountBindingHandler(PlatformAccountLinkHandler platformAccountLinkHandler) {
        registerPlatformAccountLinkHandler(platformAccountLinkHandler);
    }

    public PlatformLinkChallengeCreated createHytaleLinkChallenge(UniversalPlayerId universalPlayerId, Duration lifetime, Instant now) {
        return getPlatformAccountLinkHandler().createPlatformAccountLinkChallenge(universalPlayerId, GamePlatform.HYTALE, lifetime, now);
    }

    public PlatformAccountBinding bindHytaleAccount(UUID challengeId, String shortCode, String hytaleAccountId, String hytaleDisplayName, Instant now) {
        return getPlatformAccountLinkHandler().verifyPlatformAccountLink(challengeId, shortCode, GamePlatform.HYTALE, hytaleAccountId, hytaleDisplayName, now);
    }
}
