package com.tavall.hytale.resourcegame.middleware.platform;

import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;
import com.tavall.hytale.resourcegame.middleware.identity.PlatformAccountBinding;
import com.tavall.hytale.resourcegame.middleware.identity.PlatformAccountLinkHandler;
import com.tavall.hytale.resourcegame.middleware.identity.PlatformLinkChallengeCreated;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class MinecraftPlatformAccountBindingHandler {
    private final PlatformAccountLinkHandler platformAccountLinkHandler;

    public MinecraftPlatformAccountBindingHandler(PlatformAccountLinkHandler platformAccountLinkHandler) {
        this.platformAccountLinkHandler = platformAccountLinkHandler;
    }

    public PlatformLinkChallengeCreated createMinecraftLinkChallenge(UniversalPlayerId universalPlayerId, Duration lifetime, Instant now) {
        return platformAccountLinkHandler.createPlatformAccountLinkChallenge(universalPlayerId, GamePlatform.MINECRAFT, lifetime, now);
    }

    public PlatformAccountBinding bindMinecraftAccount(UUID challengeId, String shortCode, String minecraftAccountId, String minecraftDisplayName, Instant now) {
        return platformAccountLinkHandler.verifyPlatformAccountLink(challengeId, shortCode, GamePlatform.MINECRAFT, minecraftAccountId, minecraftDisplayName, now);
    }
}
