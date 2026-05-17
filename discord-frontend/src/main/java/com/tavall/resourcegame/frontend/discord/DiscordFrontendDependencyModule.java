package com.tavall.resourcegame.frontend.discord;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;

/**
 * Registers the Discord adapter graph behind Tavall DI tokens so the bot runtime, tests, and
 * control ingress verification share the same default-method dependency path.
 */
public final class DiscordFrontendDependencyModule {
    public void registerDependencies() {
        registerDependencies(DiscordBotConfig.fromEnvironment(System.getenv()));
    }

    public void registerDependencies(IDiscordBotConfig config) {
        registerIfMissing(IDiscordBotConfig.class, config);
        registerIfMissing(IDiscordFrontendModule.class, new DiscordFrontendModule());
        registerIfMissing(IDiscordFrontendCommandEnvelopeFactory.class, new DiscordFrontendCommandEnvelopeFactory());
        registerIfMissing(IDiscordKdCommandInputFormatterHandler.class, new DiscordKdCommandInputFormatterHandler());
        registerIfMissing(IDiscordKdCommandEnvelopeBridge.class, new DiscordKdCommandEnvelopeBridge());
        registerIfMissing(IDiscordControlCommandClient.class, new DiscordTcpControlCommandClient());
        registerIfMissing(IDiscordControlPlaneCommandBridge.class, new DiscordControlPlaneCommandBridge());
        registerIfMissing(IDiscordPermissionResolver.class, new DiscordPermissionResolver());
        registerIfMissing(IDiscordCommandPermissionHandler.class, new DiscordCommandPermissionHandler());
        registerIfMissing(IDiscordCommandTokenParser.class, new DiscordCommandTokenParser());
        registerIfMissing(IDiscordInteractionCommandHandler.class, new DiscordInteractionCommandHandler());
        registerIfMissing(IDiscordSlashCommandDefinitions.class, new DiscordSlashCommandDefinitions());
        registerIfMissing(IDiscordSlashCommandRegistrar.class, new DiscordSlashCommandRegistrar());
    }

    private <T> void registerIfMissing(Class<T> token, T instance) {
        if (!DependencyLoaderAccess.isInstanceRegistered(token)) {
            DependencyLoaderAccess.registerInstance(token, instance);
        }
    }
}
