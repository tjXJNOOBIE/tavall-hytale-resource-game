package org.tavall.control.runtime;

import org.tavall.control.common.GamePlatform;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public record ControlCommandDefinition(
        ControlCommandType commandType,
        String displayName,
        String description,
        List<ControlCommandArgumentDefinition> arguments,
        ControlCommandPermissionRequirement permissionRequirement,
        Set<CommandTargetScope> supportedTargetScopes,
        Set<GamePlatform> supportedPlatforms,
        boolean dryRunSupported,
        boolean fanout
) {
    public ControlCommandDefinition {
        Objects.requireNonNull(commandType, "commandType");
        displayName = displayName == null || displayName.isBlank() ? commandType.name() : displayName;
        description = description == null ? "" : description;
        arguments = arguments == null ? List.of() : List.copyOf(arguments);
        Objects.requireNonNull(permissionRequirement, "permissionRequirement");
        supportedTargetScopes = supportedTargetScopes == null ? Set.of(CommandTargetScope.GLOBAL) : Set.copyOf(supportedTargetScopes);
        supportedPlatforms = supportedPlatforms == null ? Set.of() : Set.copyOf(supportedPlatforms);
    }
}
