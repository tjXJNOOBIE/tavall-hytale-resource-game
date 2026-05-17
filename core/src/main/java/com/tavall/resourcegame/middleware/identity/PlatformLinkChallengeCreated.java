package com.tavall.resourcegame.middleware.identity;

public record PlatformLinkChallengeCreated(
        PlatformLinkChallenge challenge,
        String shortCode
) {
}
