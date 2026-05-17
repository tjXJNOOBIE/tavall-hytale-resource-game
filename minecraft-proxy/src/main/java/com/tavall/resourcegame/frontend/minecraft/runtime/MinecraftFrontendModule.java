package com.tavall.resourcegame.frontend.minecraft.runtime;

import com.tavall.resourcegame.api.internal.frontend.ResourceGameFrontendModuleDescriptor;
import com.tavall.resourcegame.api.internal.frontend.ResourceGameFrontendPlatform;
import com.tavall.resourcegame.api.internal.frontend.ResourceGameFrontendRuntime;

public final class MinecraftFrontendModule implements IMinecraftFrontendModule, com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete {
    private static final ResourceGameFrontendModuleDescriptor DESCRIPTOR = new ResourceGameFrontendModuleDescriptor(
            "minecraft-proxy",
            ResourceGameFrontendPlatform.MINECRAFT,
            ResourceGameFrontendRuntime.MINECRAFT_VELOCITY_PROXY_PLUGIN,
            "FrontendCommandIngressHandler",
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
