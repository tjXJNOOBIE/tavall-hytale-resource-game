package org.tavall.control.identity;

public record PlatformLinkChallengeCreated(
        PlatformLinkChallenge challenge,
        String shortCode
) {
}
