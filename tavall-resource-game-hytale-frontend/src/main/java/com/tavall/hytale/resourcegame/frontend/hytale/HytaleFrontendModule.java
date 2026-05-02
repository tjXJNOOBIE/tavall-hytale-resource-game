package com.tavall.hytale.resourcegame.frontend.hytale;

public final class HytaleFrontendModule {
    public String moduleName() {
        return "tavall-resource-game-hytale-frontend";
    }

    public String platformKey() {
        return "HYTALE";
    }

    public String implementationLanguage() {
        return "Java/Hytale-native";
    }

    public boolean ownsCanonicalGameplayState() {
        return false;
    }

    public String commandPipelineEntryPoint() {
        return "ControlCommandDispatchHandler";
    }
}
