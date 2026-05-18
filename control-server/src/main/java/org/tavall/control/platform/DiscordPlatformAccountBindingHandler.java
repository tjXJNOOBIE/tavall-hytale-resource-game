package org.tavall.control.platform;

import org.tavall.control.common.GamePlatform;
import org.tavall.control.identity.PlatformAccountBinding;
import org.tavall.control.identity.PlatformAccountLinkHandler;
import org.tavall.control.identity.PlatformLinkChallengeCreated;
import org.tavall.control.identity.UniversalPlayerId;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class DiscordPlatformAccountBindingHandler implements PlatformBindingDomain {
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
