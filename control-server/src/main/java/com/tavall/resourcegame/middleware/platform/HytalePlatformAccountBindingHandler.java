package org.tavall.control.platform;

import org.tavall.control.common.GamePlatform;
import org.tavall.control.identity.PlatformAccountBinding;
import org.tavall.control.identity.PlatformAccountLinkHandler;
import org.tavall.control.identity.PlatformLinkChallengeCreated;
import org.tavall.control.identity.UniversalPlayerId;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class HytalePlatformAccountBindingHandler implements IPlatformBindingDomain {
    public HytalePlatformAccountBindingHandler() {
    }

    public HytalePlatformAccountBindingHandler(PlatformAccountLinkHandler platformAccountLinkHandler) {
        registerPlatformAccountLinkHandler(platformAccountLinkHandler);
    }

    public PlatformLinkChallengeCreated createHytaleLinkChallenge(UniversalPlayerId universalPlayerId, Duration lifetime, Instant now) {
        return getPlatformAccountLinkHandler().createPlatformAccountLinkChallenge(universalPlayerId, GamePlatform.HYTALE, lifetime, now);
    }

    public PlatformAccountBinding bindHytaleAccount(UUID challengeId, String shortCode, String hytaleAccountId, String hytaleDisplayName, Instant now) {
        return getPlatformAccountLinkHandler().verifyPlatformAccountLink(challengeId, shortCode, GamePlatform.HYTALE, hytaleAccountId, hytaleDisplayName, now);
    }
}
