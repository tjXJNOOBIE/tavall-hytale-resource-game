package com.tavall.hytale.resourcegame.frontend.discord;

public final class DiscordFrontendModule {
    public String moduleName() {
        return "tavall-resource-game-discord-frontend";
    }

    public String platformKey() {
        return "DISCORD";
    }

    public String implementationLanguage() {
        return "Java bot/buttons";
    }

    public boolean ownsCanonicalGameplayState() {
        return false;
    }

    public String commandPipelineEntryPoint() {
        return "ControlCommandDispatchHandler";
    }
}
