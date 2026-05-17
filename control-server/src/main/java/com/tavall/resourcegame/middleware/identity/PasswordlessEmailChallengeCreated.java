package com.tavall.resourcegame.middleware.identity;

public record PasswordlessEmailChallengeCreated(
        PasswordlessEmailChallenge challenge,
        String deliveryToken
) {
}
