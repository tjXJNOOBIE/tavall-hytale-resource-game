package com.tavall.hytale.resourcegame.middleware.identity;

public record PasswordlessEmailChallengeCreated(
        PasswordlessEmailChallenge challenge,
        String deliveryToken
) {
}
