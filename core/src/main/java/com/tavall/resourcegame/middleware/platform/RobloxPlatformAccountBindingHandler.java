package com.tavall.resourcegame.middleware.platform;

import com.tavall.resourcegame.middleware.common.GamePlatform;
import com.tavall.resourcegame.middleware.identity.PlatformAccountBinding;
import com.tavall.resourcegame.middleware.identity.PlatformAccountLinkHandler;
import com.tavall.resourcegame.middleware.identity.PlatformLinkChallengeCreated;
import com.tavall.resourcegame.middleware.identity.UniversalPlayerId;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class RobloxPlatformAccountBindingHandler implements IPlatformBindingDomain {
    public RobloxPlatformAccountBindingHandler() {
    }

    public RobloxPlatformAccountBindingHandler(PlatformAccountLinkHandler platformAccountLinkHandler) {
        registerPlatformAccountLinkHandler(platformAccountLinkHandler);
    }

    public PlatformLinkChallengeCreated createRobloxLinkChallenge(UniversalPlayerId universalPlayerId, Duration lifetime, Instant now) {
        return getPlatformAccountLinkHandler().createPlatformAccountLinkChallenge(universalPlayerId, GamePlatform.ROBLOX, lifetime, now);
    }

    public PlatformAccountBinding bindRobloxAccount(UUID challengeId, String shortCode, String robloxUserId, String robloxDisplayName, Instant now) {
        return getPlatformAccountLinkHandler().verifyPlatformAccountLink(challengeId, shortCode, GamePlatform.ROBLOX, robloxUserId, robloxDisplayName, now);
    }
}
