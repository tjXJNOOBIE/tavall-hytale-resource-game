package com.tavall.hytale.resourcegame.frontend.minecraft;

import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendModuleDescriptor;
import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendPlatform;
import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendRuntime;

public final class MinecraftFrontendModule {
    private static final ResourceGameFrontendModuleDescriptor DESCRIPTOR = new ResourceGameFrontendModuleDescriptor(
            "tavall-resource-game-minecraft-frontend",
            ResourceGameFrontendPlatform.MINECRAFT,
            ResourceGameFrontendRuntime.MINECRAFT_JAVA_PLUGIN,
            "ControlCommandDispatchHandler",
            false
    );

    public ResourceGameFrontendModuleDescriptor descriptor() {
        return DESCRIPTOR;
    }

    public String moduleName() {
        return DESCRIPTOR.moduleName();
    }

    public String platformKey() {
        return DESCRIPTOR.platformKey();
    }

    public String implementationLanguage() {
        return DESCRIPTOR.runtime().name();
    }

    public boolean ownsCanonicalGameplayState() {
        return DESCRIPTOR.ownsCanonicalGameplayState();
    }

    public String commandPipelineEntryPoint() {
        return DESCRIPTOR.commandPipelineEntryPoint();
    }
}
