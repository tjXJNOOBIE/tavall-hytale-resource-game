package com.tavall.resourcegame.api.internal.frontend;

import java.util.Objects;

public record ResourceGameFrontendModuleDescriptor(
        String moduleName,
        ResourceGameFrontendPlatform platform,
        ResourceGameFrontendRuntime runtime,
        String commandPipelineEntryPoint,
        boolean ownsCanonicalGameplayState
) {
    public ResourceGameFrontendModuleDescriptor {
        Objects.requireNonNull(moduleName, "moduleName");
        Objects.requireNonNull(platform, "platform");
        Objects.requireNonNull(runtime, "runtime");
        Objects.requireNonNull(commandPipelineEntryPoint, "commandPipelineEntryPoint");

        if (moduleName.isBlank()) {
            throw new IllegalArgumentException("moduleName must not be blank");
        }

        if (commandPipelineEntryPoint.isBlank()) {
            throw new IllegalArgumentException("commandPipelineEntryPoint must not be blank");
        }
    }

    public String platformKey() {
        return platform.name();
    }
}
