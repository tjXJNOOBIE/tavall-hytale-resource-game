package com.tavall.hytale.resourcegame.frontend.minecraft;

import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandVerificationResult;

public record MinecraftVelocityCommandResult(boolean success, String message) {
    public static MinecraftVelocityCommandResult denied(String message) {
        return new MinecraftVelocityCommandResult(false, message);
    }

    public static MinecraftVelocityCommandResult fromVerification(FrontendCommandVerificationResult result) {
        String commandState = result.controlCommandState() == null ? result.state().name() : result.controlCommandState();
        return new MinecraftVelocityCommandResult(
                result.success(),
                commandState + ": " + result.message()
        );
    }
}
