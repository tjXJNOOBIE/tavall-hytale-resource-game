package com.tavall.resourcegame.frontend.minecraft;

import com.tavall.resourcegame.api.internal.frontend.FrontendCommandVerificationResult;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class MinecraftVelocityCommandExecutionHandler implements IMinecraftVelocityCommandExecutionHandler, IMinecraftFrontendDomain, IDependencyInjectableConcrete {
    @Override
    public MinecraftVelocityCommandResult execute(
            MinecraftVelocityCommandSource source,
            String alias,
            String[] arguments
    ) {
        if (!getMinecraftVelocityCommandPermissionHandler().canExecute(source, alias, arguments)) {
            String permission = getMinecraftVelocityCommandPermissionHandler().requiresAdminPermission(alias, arguments)
                    ? getMinecraftVelocityCommandPermissionHandler().adminPermission()
                    : getMinecraftVelocityCommandPermissionHandler().commandPermission();
            return MinecraftVelocityCommandResult.denied("Missing permission " + permission + ".");
        }
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("proxy", getMinecraftProxyConfig().serverId());
        metadata.put("surfaceIdentity", "VELOCITY_PROXY");
        metadata.put("serverDataSource", "bukkit-server-snapshot-ingress");
        metadata.put("alias", alias);
        metadata.put("sourceType", source.sourceType());
        metadata.putAll(source.metadata());
        FrontendCommandVerificationResult result = getMinecraftControlPlaneCommandBridge().submitKdCommand(
                source.platformAccountId(),
                source.platformDisplayName(),
                commandTokens(alias, arguments),
                "minecraft-velocity-" + UUID.randomUUID(),
                metadata
        );
        if (result.success() && result.metadata().containsKey("instanceSwitchRequestId")) {
            return getMinecraftVelocityInstanceSwitchHandler().dispatchSwitchIfPresent(source, result);
        }
        return MinecraftVelocityCommandResult.fromVerification(result);
    }

    private java.util.List<String> commandTokens(String alias, String[] arguments) {
        java.util.ArrayList<String> tokens = new java.util.ArrayList<>();
        tokens.add(alias);
        tokens.addAll(Arrays.asList(arguments));
        return java.util.List.copyOf(tokens);
    }
}
