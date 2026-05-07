package com.tavall.hytale.resourcegame.middleware.platform;

import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;
import com.tavall.hytale.resourcegame.middleware.identity.PlatformAccountBinding;
import com.tavall.hytale.resourcegame.middleware.identity.PlatformAccountLinkHandler;
import com.tavall.hytale.resourcegame.middleware.identity.PlatformLinkChallengeCreated;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class DiscordPlatformAccountBindingHandler {
    private final PlatformAccountLinkHandler platformAccountLinkHandler;

    public DiscordPlatformAccountBindingHandler(PlatformAccountLinkHandler platformAccountLinkHandler) {
        this.platformAccountLinkHandler = platformAccountLinkHandler;
    }

    public PlatformLinkChallengeCreated createDiscordLinkChallenge(UniversalPlayerId universalPlayerId, Duration lifetime, Instant now) {
        return platformAccountLinkHandler.createPlatformAccountLinkChallenge(universalPlayerId, GamePlatform.DISCORD, lifetime, now);
    }

    public PlatformAccountBinding bindDiscordAccount(UUID challengeId, String shortCode, String discordUserId, String discordDisplayName, Instant now) {
        return platformAccountLinkHandler.verifyPlatformAccountLink(challengeId, shortCode, GamePlatform.DISCORD, discordUserId, discordDisplayName, now);
    }
}
