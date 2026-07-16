package org.tavall.control.identity;

public record PasswordlessEmailChallengeCreated(
        PasswordlessEmailChallenge challenge,
        String deliveryToken
) {
}
