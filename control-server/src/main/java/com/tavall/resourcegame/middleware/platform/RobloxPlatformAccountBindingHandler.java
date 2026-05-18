package org.tavall.control.platform;

import org.tavall.control.common.GamePlatform;
import org.tavall.control.identity.PlatformAccountBinding;
import org.tavall.control.identity.PlatformAccountLinkHandler;
import org.tavall.control.identity.PlatformLinkChallengeCreated;
import org.tavall.control.identity.UniversalPlayerId;

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
