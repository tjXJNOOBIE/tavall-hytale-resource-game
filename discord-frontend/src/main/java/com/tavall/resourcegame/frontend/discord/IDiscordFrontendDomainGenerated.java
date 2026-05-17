package com.tavall.resourcegame.frontend.discord;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;

public interface IDiscordFrontendDomainGenerated {
    default IDiscordBotConfig getDiscordBotConfig() {
        return DependencyLoaderAccess.requireInstance(IDiscordBotConfig.class);
    }

    default IDiscordFrontendModule getDiscordFrontendModule() {
        return DependencyLoaderAccess.requireInstance(IDiscordFrontendModule.class);
    }

    default IDiscordFrontendCommandEnvelopeFactory getDiscordFrontendCommandEnvelopeFactory() {
        return DependencyLoaderAccess.requireInstance(IDiscordFrontendCommandEnvelopeFactory.class);
    }

    default IDiscordKdCommandInputFormatterHandler getDiscordKdCommandInputFormatterHandler() {
        return DependencyLoaderAccess.requireInstance(IDiscordKdCommandInputFormatterHandler.class);
    }

    default IDiscordKdCommandEnvelopeBridge getDiscordKdCommandEnvelopeBridge() {
        return DependencyLoaderAccess.requireInstance(IDiscordKdCommandEnvelopeBridge.class);
    }

    default IDiscordControlCommandClient getDiscordControlCommandClient() {
        return DependencyLoaderAccess.requireInstance(IDiscordControlCommandClient.class);
    }

    default IDiscordControlPlaneCommandBridge getDiscordControlPlaneCommandBridge() {
        return DependencyLoaderAccess.requireInstance(IDiscordControlPlaneCommandBridge.class);
    }

    default IDiscordPermissionResolver getDiscordPermissionResolver() {
        return DependencyLoaderAccess.requireInstance(IDiscordPermissionResolver.class);
    }

    default IDiscordCommandPermissionHandler getDiscordCommandPermissionHandler() {
        return DependencyLoaderAccess.requireInstance(IDiscordCommandPermissionHandler.class);
    }

    default IDiscordCommandTokenParser getDiscordCommandTokenParser() {
        return DependencyLoaderAccess.requireInstance(IDiscordCommandTokenParser.class);
    }

    default IDiscordInteractionCommandHandler getDiscordInteractionCommandHandler() {
        return DependencyLoaderAccess.requireInstance(IDiscordInteractionCommandHandler.class);
    }

    default IDiscordSlashCommandDefinitions getDiscordSlashCommandDefinitions() {
        return DependencyLoaderAccess.requireInstance(IDiscordSlashCommandDefinitions.class);
    }

    default IDiscordSlashCommandRegistrar getDiscordSlashCommandRegistrar() {
        return DependencyLoaderAccess.requireInstance(IDiscordSlashCommandRegistrar.class);
    }
}
