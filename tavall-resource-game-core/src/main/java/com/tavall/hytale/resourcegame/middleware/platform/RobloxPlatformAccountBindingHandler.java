package com.tavall.hytale.resourcegame.middleware.platform;

import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;
import com.tavall.hytale.resourcegame.middleware.identity.PlatformAccountBinding;
import com.tavall.hytale.resourcegame.middleware.identity.PlatformAccountLinkHandler;
import com.tavall.hytale.resourcegame.middleware.identity.PlatformLinkChallengeCreated;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class RobloxPlatformAccountBindingHandler {
    private final PlatformAccountLinkHandler platformAccountLinkHandler;

    public RobloxPlatformAccountBindingHandler(PlatformAccountLinkHandler platformAccountLinkHandler) {
        this.platformAccountLinkHandler = platformAccountLinkHandler;
    }

    public PlatformLinkChallengeCreated createRobloxLinkChallenge(UniversalPlayerId universalPlayerId, Duration lifetime, Instant now) {
        return platformAccountLinkHandler.createPlatformAccountLinkChallenge(universalPlayerId, GamePlatform.ROBLOX, lifetime, now);
    }

    public PlatformAccountBinding bindRobloxAccount(UUID challengeId, String shortCode, String robloxUserId, String robloxDisplayName, Instant now) {
        return platformAccountLinkHandler.verifyPlatformAccountLink(challengeId, shortCode, GamePlatform.ROBLOX, robloxUserId, robloxDisplayName, now);
    }
}
