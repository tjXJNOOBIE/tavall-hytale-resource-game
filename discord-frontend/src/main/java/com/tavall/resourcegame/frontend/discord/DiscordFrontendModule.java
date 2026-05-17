package com.tavall.resourcegame.frontend.discord;

import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendModuleDescriptor;
import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendPlatform;
import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendRuntime;

public final class DiscordFrontendModule implements IDiscordFrontendModule, com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete {
    private static final ResourceGameFrontendModuleDescriptor DESCRIPTOR = new ResourceGameFrontendModuleDescriptor(
            "discord-frontend",
            ResourceGameFrontendPlatform.DISCORD,
            ResourceGameFrontendRuntime.DISCORD_JAVA_BOT,
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
