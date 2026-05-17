package com.tavall.resourcegame.middleware.platform;

import com.tavall.resourcegame.middleware.common.GamePlatform;
import com.tavall.resourcegame.middleware.identity.PlatformAccountBinding;
import com.tavall.resourcegame.middleware.identity.PlatformAccountLinkHandler;
import com.tavall.resourcegame.middleware.identity.PlatformLinkChallengeCreated;
import com.tavall.resourcegame.middleware.identity.UniversalPlayerId;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class MinecraftPlatformAccountBindingHandler implements IPlatformBindingDomain {
    public MinecraftPlatformAccountBindingHandler() {
    }

    public MinecraftPlatformAccountBindingHandler(PlatformAccountLinkHandler platformAccountLinkHandler) {
        registerPlatformAccountLinkHandler(platformAccountLinkHandler);
    }

    public PlatformLinkChallengeCreated createMinecraftLinkChallenge(UniversalPlayerId universalPlayerId, Duration lifetime, Instant now) {
        return getPlatformAccountLinkHandler().createPlatformAccountLinkChallenge(universalPlayerId, GamePlatform.MINECRAFT, lifetime, now);
    }

    public PlatformAccountBinding bindMinecraftAccount(UUID challengeId, String shortCode, String minecraftAccountId, String minecraftDisplayName, Instant now) {
        return getPlatformAccountLinkHandler().verifyPlatformAccountLink(challengeId, shortCode, GamePlatform.MINECRAFT, minecraftAccountId, minecraftDisplayName, now);
    }
}
