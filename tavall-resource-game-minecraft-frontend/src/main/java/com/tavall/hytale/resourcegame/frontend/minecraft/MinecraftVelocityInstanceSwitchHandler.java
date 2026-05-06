package com.tavall.hytale.resourcegame.frontend.minecraft;

import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandVerificationResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionException;

public final class MinecraftVelocityInstanceSwitchHandler {
    private final MinecraftVelocityInstanceSwitchGateway switchGateway;
    private final MinecraftControlPlaneCommandBridge commandBridge;

    public MinecraftVelocityInstanceSwitchHandler(MinecraftVelocityInstanceSwitchGateway switchGateway, MinecraftControlPlaneCommandBridge commandBridge) {
        this.switchGateway = switchGateway;
        this.commandBridge = commandBridge;
    }

    public static MinecraftVelocityInstanceSwitchHandler noop() {
        return new MinecraftVelocityInstanceSwitchHandler(MinecraftVelocityInstanceSwitchGateway.noop(), null);
    }

    public MinecraftVelocityCommandResult dispatchSwitchIfPresent(MinecraftVelocityCommandSource source, FrontendCommandVerificationResult switchRequestResult) {
        String switchRequestId = switchRequestResult.metadata().get("instanceSwitchRequestId");
        if (switchRequestId == null || switchRequestId.isBlank()) {
            return MinecraftVelocityCommandResult.fromVerification(switchRequestResult);
        }
        String targetInstanceId = parseTargetInstanceId(switchRequestResult);
        MinecraftVelocityInstanceSwitchGateway.MinecraftVelocityInstanceSwitchResult switchResult;
        try {
            switchResult = switchGateway.switchPlayer(source.platformAccountId(), targetInstanceId).join();
        } catch (CompletionException exception) {
            switchResult = MinecraftVelocityInstanceSwitchGateway.MinecraftVelocityInstanceSwitchResult.failed(targetInstanceId, exception.getMessage());
        }

        if (commandBridge == null) {
            return new MinecraftVelocityCommandResult(switchResult.success(), switchResult.message());
        }
        FrontendCommandVerificationResult completionResult = switchResult.success()
                ? submitCompletion(source, List.of("kd", "instance", "confirm", switchRequestId), "minecraft-velocity-switch-confirm-" + UUID.randomUUID(), switchResult)
                : submitCompletion(source, List.of("kd", "instance", "fail", switchRequestId, sanitizeReason(switchResult.message())), "minecraft-velocity-switch-fail-" + UUID.randomUUID(), switchResult);
        return new MinecraftVelocityCommandResult(
                switchResult.success() && completionResult.success(),
                MinecraftVelocityCommandResult.fromVerification(switchRequestResult).message() + " " + switchResult.message() + " " + MinecraftVelocityCommandResult.fromVerification(completionResult).message()
        );
    }

    private FrontendCommandVerificationResult submitCompletion(
            MinecraftVelocityCommandSource source,
            List<String> commandTokens,
            String correlationId,
            MinecraftVelocityInstanceSwitchGateway.MinecraftVelocityInstanceSwitchResult switchResult
    ) {
        return commandBridge.submitKdCommand(
                source.platformAccountId(),
                source.platformDisplayName(),
                commandTokens,
                correlationId,
                Map.of(
                        "sourceType", source.sourceType(),
                        "velocitySwitchTarget", switchResult.targetServerName().orElse(""),
                        "velocitySwitchAttempted", Boolean.toString(switchResult.attempted())
                )
        );
    }

    private String parseTargetInstanceId(FrontendCommandVerificationResult switchRequestResult) {
        String changedObjectIds = switchRequestResult.metadata().getOrDefault("changedObjectIds", "");
        for (String changedObjectId : changedObjectIds.split(",")) {
            if (changedObjectId.startsWith("platform-instance:")) {
                return changedObjectId.substring("platform-instance:".length());
            }
        }
        return switchRequestResult.metadata().getOrDefault("toInstanceId", switchRequestResult.metadata().getOrDefault("toKingdomId", ""));
    }

    private String sanitizeReason(String message) {
        return message == null || message.isBlank() ? "velocity_switch_failed" : message.replaceAll("[^A-Za-z0-9_.-]+", "_");
    }
}
