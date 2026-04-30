package com.tavall.hytale.resourcegame.middleware.security;

import java.util.List;

public record GeneratedRecoveryCodes(
        List<TwoFactorRecoveryCode> storedRecoveryCodes,
        List<String> displayCodes
) {
    public GeneratedRecoveryCodes {
        storedRecoveryCodes = List.copyOf(storedRecoveryCodes);
        displayCodes = List.copyOf(displayCodes);
    }
}
