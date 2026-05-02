package com.tavall.hytale.resourcegame.frontend.minecraft;

public final class MinecraftFrontendModule {
    public String moduleName() {
        return "tavall-resource-game-minecraft-frontend";
    }

    public String platformKey() {
        return "MINECRAFT";
    }

    public String implementationLanguage() {
        return "Java plugin/native adapter";
    }

    public boolean ownsCanonicalGameplayState() {
        return false;
    }

    public String commandPipelineEntryPoint() {
        return "ControlCommandDispatchHandler";
    }
}
