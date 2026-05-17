package com.tavall.resourcegame.frontend.discord;

import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendModuleDescriptor;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IDiscordFrontendModule extends IDependencyInjectableInterface {
    ResourceGameFrontendModuleDescriptor descriptor();

    String moduleName();

    String platformKey();

    String implementationLanguage();

    boolean ownsCanonicalGameplayState();

    String commandPipelineEntryPoint();
}
