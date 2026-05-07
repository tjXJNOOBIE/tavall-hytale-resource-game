package com.tavall.hytale.resourcegame.middleware.identity;

public record PlatformLinkChallengeCreated(
        PlatformLinkChallenge challenge,
        String shortCode
) {
}
