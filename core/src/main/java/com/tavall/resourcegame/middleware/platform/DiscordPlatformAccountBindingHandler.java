package com.tavall.resourcegame.middleware.platform;

import com.tavall.resourcegame.middleware.common.GamePlatform;
import com.tavall.resourcegame.middleware.identity.PlatformAccountBinding;
import com.tavall.resourcegame.middleware.identity.PlatformAccountLinkHandler;
import com.tavall.resourcegame.middleware.identity.PlatformLinkChallengeCreated;
import com.tavall.resourcegame.middleware.identity.UniversalPlayerId;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class DiscordPlatformAccountBindingHandler implements IPlatformBindingDomain {
    public DiscordPlatformAccountBindingHandler() {
    }

    public DiscordPlatformAccountBindingHandler(PlatformAccountLinkHandler platformAccountLinkHandler) {
        registerPlatformAccountLinkHandler(platformAccountLinkHandler);
    }

    public PlatformLinkChallengeCreated createDiscordLinkChallenge(UniversalPlayerId universalPlayerId, Duration lifetime, Instant now) {
        return getPlatformAccountLinkHandler().createPlatformAccountLinkChallenge(universalPlayerId, GamePlatform.DISCORD, lifetime, now);
    }

    public PlatformAccountBinding bindDiscordAccount(UUID challengeId, String shortCode, String discordUserId, String discordDisplayName, Instant now) {
        return getPlatformAccountLinkHandler().verifyPlatformAccountLink(challengeId, shortCode, GamePlatform.DISCORD, discordUserId, discordDisplayName, now);
    }
}
