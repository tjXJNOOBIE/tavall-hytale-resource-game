package org.tavall.minecraft.switching;

import org.tavall.api.minecraft.frontend.FrontendCommandVerificationResult;
import org.tavall.minecraft.commands.source.MinecraftVelocityCommandSource;
import org.tavall.minecraft.routing.MinecraftVelocityCommandResult;
import org.tavall.minecraft.runtime.IMinecraftFrontendDomain;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionException;

public final class MinecraftVelocityInstanceSwitchHandler implements IMinecraftVelocityInstanceSwitchHandler, IMinecraftFrontendDomain, IDependencyInjectableConcrete {
    @Override
    public MinecraftVelocityCommandResult dispatchSwitchIfPresent(MinecraftVelocityCommandSource source, FrontendCommandVerificationResult switchRequestResult) {
        String switchRequestId = switchRequestResult.metadata().get("instanceSwitchRequestId");
        if (switchRequestId == null || switchRequestId.isBlank()) {
            return MinecraftVelocityCommandResult.fromVerification(switchRequestResult);
        }
        String targetInstanceId = parseTargetInstanceId(switchRequestResult);
        MinecraftVelocityInstanceSwitchGateway.MinecraftVelocityInstanceSwitchResult switchResult;
        try {
            switchResult = getMinecraftVelocityInstanceSwitchGateway().switchPlayer(source.platformAccountId(), targetInstanceId).join();
        } catch (CompletionException exception) {
            switchResult = MinecraftVelocityInstanceSwitchGateway.MinecraftVelocityInstanceSwitchResult.failed(targetInstanceId, exception.getMessage());
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
        return getMinecraftControlPlaneCommandBridge().submitKdCommand(
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
