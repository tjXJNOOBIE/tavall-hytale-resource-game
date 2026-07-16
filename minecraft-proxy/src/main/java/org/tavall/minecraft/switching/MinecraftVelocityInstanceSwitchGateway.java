package org.tavall.minecraft.switching;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface MinecraftVelocityInstanceSwitchGateway extends IMinecraftVelocityInstanceSwitchGateway, IDependencyInjectableConcrete {
    @Override
    CompletableFuture<MinecraftVelocityInstanceSwitchResult> switchPlayer(String platformAccountId, String targetInstanceId);

    static MinecraftVelocityInstanceSwitchGateway noop() {
        return (platformAccountId, targetInstanceId) -> CompletableFuture.completedFuture(
                MinecraftVelocityInstanceSwitchResult.notAttempted("No Velocity instance switch gateway is configured.")
        );
    }

    record MinecraftVelocityInstanceSwitchResult(boolean attempted, boolean success, Optional<String> targetServerName, String message) {
        public MinecraftVelocityInstanceSwitchResult {
            targetServerName = targetServerName == null ? Optional.empty() : targetServerName;
            message = message == null ? "" : message;
        }

        public static MinecraftVelocityInstanceSwitchResult success(String targetServerName) {
            return new MinecraftVelocityInstanceSwitchResult(true, true, Optional.of(targetServerName), "Velocity switch connected to " + targetServerName + ".");
        }

        public static MinecraftVelocityInstanceSwitchResult failed(String targetServerName, String message) {
            return new MinecraftVelocityInstanceSwitchResult(true, false, Optional.ofNullable(targetServerName), message);
        }

        public static MinecraftVelocityInstanceSwitchResult notAttempted(String message) {
            return new MinecraftVelocityInstanceSwitchResult(false, false, Optional.empty(), message);
        }
    }
}
