package org.tavall.minecraft.bootstrap;

import org.tavall.minecraft.commands.IGuild;
import org.tavall.minecraft.commands.ISim;
import org.tavall.dependency.DependencyLoaderAccess;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.util.Objects;

public final class MinecraftVelocityBootstrap {
    private final ProxyServer proxyServer;
    private final Logger logger;
    private final MinecraftProxyConfig config;
    private final Object pluginOwner;

    public MinecraftVelocityBootstrap(ProxyServer proxyServer, Logger logger, MinecraftProxyConfig config, Object pluginOwner) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.config = config;
        this.pluginOwner = pluginOwner;
    }

    public void initialize() {
        registerProxyServer();
        new MinecraftFrontendDependencyModule().registerDependencies(config);
        registerCommands();
        logger.info("Registered Tavall Resource Game Velocity routing adapter. serverId={}", config.serverId());
    }

    private void registerProxyServer() {
        if (DependencyLoaderAccess.isInstanceRegistered(ProxyServer.class)) {
            DependencyLoaderAccess.replaceInstance(ProxyServer.class, () -> proxyServer);
        } else {
            DependencyLoaderAccess.registerInstance(ProxyServer.class, proxyServer);
        }
    }

    private void registerCommands() {
        CommandManager commandManager = proxyServer.getCommandManager();
        ISim simCommand = Objects.requireNonNull(DependencyLoaderAccess.findInstance(ISim.class),
                "Expected ISim to be registered in the DI map.");
        IGuild guildCommand = Objects.requireNonNull(DependencyLoaderAccess.findInstance(IGuild.class),
                "Expected IGuild to be registered in the DI map.");
        commandManager.register(commandManager.metaBuilder("sim").plugin(pluginOwner).build(), simCommand);
        BrigadierCommand brigadierGuildCommand = guildCommand.brigadierCommand();
        commandManager.register(commandManager.metaBuilder("guild").plugin(pluginOwner).build(), brigadierGuildCommand);
        logger.info("Registered Velocity command /sim");
        logger.info("Registered Velocity command /guild");
    }
}

