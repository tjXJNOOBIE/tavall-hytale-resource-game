package org.tavall.minecraft.switching;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.util.concurrent.CompletableFuture;

public interface IMinecraftVelocityInstanceSwitchGateway extends IDependencyInjectableInterface {
    CompletableFuture<MinecraftVelocityInstanceSwitchGateway.MinecraftVelocityInstanceSwitchResult> switchPlayer(
            String platformAccountId,
            String targetInstanceId
    );
}
