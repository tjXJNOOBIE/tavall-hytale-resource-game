package com.tavall.hytale.resourcegame.middleware.platform;

import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;
import com.tavall.hytale.resourcegame.middleware.identity.PlatformAccountBinding;
import com.tavall.hytale.resourcegame.middleware.identity.PlatformAccountLinkHandler;
import com.tavall.hytale.resourcegame.middleware.identity.PlatformLinkChallengeCreated;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class HytalePlatformAccountBindingHandler {
    private final PlatformAccountLinkHandler platformAccountLinkHandler;

    public HytalePlatformAccountBindingHandler(PlatformAccountLinkHandler platformAccountLinkHandler) {
        this.platformAccountLinkHandler = platformAccountLinkHandler;
    }

    public PlatformLinkChallengeCreated createHytaleLinkChallenge(UniversalPlayerId universalPlayerId, Duration lifetime, Instant now) {
        return platformAccountLinkHandler.createPlatformAccountLinkChallenge(universalPlayerId, GamePlatform.HYTALE, lifetime, now);
    }

    public PlatformAccountBinding bindHytaleAccount(UUID challengeId, String shortCode, String hytaleAccountId, String hytaleDisplayName, Instant now) {
        return platformAccountLinkHandler.verifyPlatformAccountLink(challengeId, shortCode, GamePlatform.HYTALE, hytaleAccountId, hytaleDisplayName, now);
    }
}
