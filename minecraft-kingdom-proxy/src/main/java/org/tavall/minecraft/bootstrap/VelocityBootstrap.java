package org.tavall.minecraft.bootstrap;

import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

public final class VelocityBootstrap implements org.tavall.minecraft.permissions.IVelocityDependencyAccess {
    private final ProxyServer proxyServer;
    private final Logger logger;
    private final VelocityProxyConfig config;
    private final Object pluginOwner;

    public VelocityBootstrap(ProxyServer proxyServer, Logger logger, VelocityProxyConfig config, Object pluginOwner) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.config = config;
        this.pluginOwner = pluginOwner;
    }

    public void initialize() {
        registerProxyServer();
        new VelocityDependencyModule().registerDependencies(config);
        registerCommands();
        logger.info("Registered Tavall Resource Game Velocity command surface. serverId={}", config.serverId());
    }

    private void registerProxyServer() {
        if (org.tavall.dependency.DependencyLoaderAccess.isInstanceRegistered(ProxyServer.class)) {
            org.tavall.dependency.DependencyLoaderAccess.replaceInstance(ProxyServer.class, () -> proxyServer);
        } else {
            org.tavall.dependency.DependencyLoaderAccess.registerInstance(ProxyServer.class, proxyServer);
        }
    }

    private void registerCommands() {
        CommandManager commandManager = proxyServer.getCommandManager();
        org.tavall.minecraft.commands.ISim simCommand = getSimCommand();
        org.tavall.minecraft.commands.IGuild guildCommand = getGuildCommand();
        org.tavall.minecraft.commands.IRank rankCommand = getRankCommand();
        commandManager.register(commandManager.metaBuilder("sim").plugin(pluginOwner).build(), simCommand);
        BrigadierCommand brigadierGuildCommand = guildCommand.brigadierCommand();
        commandManager.register(commandManager.metaBuilder("guild").plugin(pluginOwner).build(), brigadierGuildCommand);
        commandManager.register(commandManager.metaBuilder("rank").plugin(pluginOwner).build(), rankCommand);
        logger.info("Registered Velocity command /sim");
        logger.info("Registered Velocity command /guild");
        logger.info("Registered Velocity command /rank");
    }
}
