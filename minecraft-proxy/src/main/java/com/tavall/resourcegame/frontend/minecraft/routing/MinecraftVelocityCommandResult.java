package com.tavall.resourcegame.frontend.minecraft.routing;

import com.tavall.resourcegame.api.internal.frontend.FrontendCommandVerificationResult;

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
