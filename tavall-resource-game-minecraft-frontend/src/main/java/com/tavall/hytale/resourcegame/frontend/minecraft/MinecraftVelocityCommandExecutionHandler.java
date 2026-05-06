package com.tavall.hytale.resourcegame.frontend.minecraft;

import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandVerificationResult;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class MinecraftVelocityCommandExecutionHandler {
    private final MinecraftControlPlaneCommandBridge commandBridge;
    private final MinecraftVelocityCommandPermissionHandler permissionHandler;
    private final String serverId;

    public MinecraftVelocityCommandExecutionHandler(
            MinecraftControlPlaneCommandBridge commandBridge,
            MinecraftVelocityCommandPermissionHandler permissionHandler,
            String serverId
    ) {
        this.commandBridge = commandBridge;
        this.permissionHandler = permissionHandler;
        this.serverId = serverId;
    }

    public MinecraftVelocityCommandResult execute(
            MinecraftVelocityCommandSource source,
            String alias,
            String[] arguments
    ) {
        if (!permissionHandler.canExecute(source, alias, arguments)) {
            String permission = permissionHandler.requiresAdminPermission(alias, arguments)
                    ? permissionHandler.adminPermission()
                    : permissionHandler.commandPermission();
            return MinecraftVelocityCommandResult.denied("Missing permission " + permission + ".");
        }
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("proxy", serverId);
        metadata.put("alias", alias);
        metadata.put("sourceType", source.sourceType());
        metadata.putAll(source.metadata());
        FrontendCommandVerificationResult result = commandBridge.submitKdCommand(
                source.platformAccountId(),
                source.platformDisplayName(),
                commandTokens(alias, arguments),
                "minecraft-velocity-" + UUID.randomUUID(),
                metadata
        );
        return MinecraftVelocityCommandResult.fromVerification(result);
    }

    private java.util.List<String> commandTokens(String alias, String[] arguments) {
        java.util.ArrayList<String> tokens = new java.util.ArrayList<>();
        tokens.add(alias);
        tokens.addAll(Arrays.asList(arguments));
        return java.util.List.copyOf(tokens);
    }
}
