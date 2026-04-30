package com.tavall.hytale.resourcegame.middleware.control;

import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;
import com.tavall.hytale.resourcegame.middleware.common.MetadataMaps;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record ControlCommand(
        ControlCommandId commandId,
        ControlCommandType commandType,
        ControlOperator issuedBy,
        CommandIssuedFrom issuedFrom,
        CommandTargetScope targetScope,
        Set<GamePlatform> targetPlatforms,
        Map<String, String> arguments,
        boolean dryRun,
        Instant createdAt,
        Map<String, String> metadata
) {
    public ControlCommand {
        Objects.requireNonNull(commandId, "commandId");
        Objects.requireNonNull(commandType, "commandType");
        Objects.requireNonNull(issuedBy, "issuedBy");
        issuedFrom = issuedFrom == null ? CommandIssuedFrom.SYSTEM : issuedFrom;
        targetScope = targetScope == null ? CommandTargetScope.GLOBAL : targetScope;
        targetPlatforms = targetPlatforms == null ? Set.of() : Set.copyOf(targetPlatforms);
        arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
        Objects.requireNonNull(createdAt, "createdAt");
        metadata = MetadataMaps.immutable(metadata);
    }

    public String argument(String name) {
        String value = arguments.get(name);
        if (value == null || value.isBlank()) {
            throw new ControlCommandValidationException("Missing required argument: " + name + ".");
        }
        return value;
    }
}
