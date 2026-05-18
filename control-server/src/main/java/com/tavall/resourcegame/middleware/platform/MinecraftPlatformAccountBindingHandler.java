package org.tavall.control.platform;

import org.tavall.control.common.GamePlatform;
import org.tavall.control.identity.PlatformAccountBinding;
import org.tavall.control.identity.PlatformAccountLinkHandler;
import org.tavall.control.identity.PlatformLinkChallengeCreated;
import org.tavall.control.identity.UniversalPlayerId;

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
