package org.tavall.minecraft.runtime;

import org.tavall.api.minecraft.frontend.ResourceGameFrontendModuleDescriptor;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendPlatform;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendRuntime;

public final class MinecraftFrontendModule implements IMinecraftFrontendModule, org.tavall.dependency.IDependencyInjectableConcrete {
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
