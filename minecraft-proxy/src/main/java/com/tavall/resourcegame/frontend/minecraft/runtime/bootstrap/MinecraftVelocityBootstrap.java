package com.tavall.resourcegame.frontend.minecraft.runtime.bootstrap;

import com.tavall.resourcegame.frontend.minecraft.runtime.MinecraftVelocityLoginEvent;
import com.tavall.resourcegame.frontend.minecraft.runtime.MinecraftVelocityMuteChatEvent;
import com.tavall.resourcegame.frontend.minecraft.commands.Ban;
import com.tavall.resourcegame.frontend.minecraft.commands.Kick;
import com.tavall.resourcegame.frontend.minecraft.commands.MinecraftVelocityRankCommand;
import com.tavall.resourcegame.frontend.minecraft.commands.Mute;
import com.tavall.resourcegame.frontend.minecraft.commands.Sim;
import com.tavall.resourcegame.frontend.minecraft.commands.Unban;
import com.tavall.resourcegame.frontend.minecraft.commands.Warn;
import com.tavall.resourcegame.frontend.minecraft.runtime.MinecraftFrontendDependencyModule;
import com.tavall.resourcegame.frontend.minecraft.runtime.MinecraftProxyConfig;
import com.tavall.resourcegame.frontend.minecraft.runtime.IMinecraftVelocityProxyServer;
import com.tavall.resourcegame.frontend.minecraft.runtime.MinecraftVelocityProxyServerAdapter;
import com.tavall.resourcegame.frontend.minecraft.switching.MinecraftVelocityInstanceSwitchGateway;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

public final class MinecraftVelocityBootstrap {
    private final ProxyServer proxyServer;
    private final Logger logger;
    private final MinecraftProxyConfig config;
    private final MinecraftVelocityInstanceSwitchGateway switchGateway;

    public MinecraftVelocityBootstrap(ProxyServer proxyServer, Logger logger, MinecraftProxyConfig config, MinecraftVelocityInstanceSwitchGateway switchGateway) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.config = config;
        this.switchGateway = switchGateway;
    }

    public void initialize() {
        registerProxyServerAdapter();
        new MinecraftFrontendDependencyModule().registerDependencies(config, switchGateway);
        registerEvents();
        registerCommands();
        logger.info("Registered Tavall Resource Game Velocity routing adapter. serverId={}", config.serverId());
    }

    private void registerProxyServerAdapter() {
        if (!DependencyLoaderAccess.isInstanceRegistered(IMinecraftVelocityProxyServer.class)) {
            DependencyLoaderAccess.registerInstance(IMinecraftVelocityProxyServer.class, new MinecraftVelocityProxyServerAdapter(proxyServer));
        }
    }

    private void registerEvents() {
        proxyServer.getEventManager().register(this, new MinecraftVelocityLoginEvent());
        proxyServer.getEventManager().register(this, new MinecraftVelocityMuteChatEvent());
    }

    private void registerCommands() {
        CommandManager commandManager = proxyServer.getCommandManager();
        commandManager.register(commandManager.metaBuilder("rank").plugin(this).build(), new MinecraftVelocityRankCommand());
        logger.info("Registered Velocity command /rank");
        commandManager.register(commandManager.metaBuilder("ban").plugin(this).build(), new Ban());
        logger.info("Registered Velocity command /ban");
        commandManager.register(commandManager.metaBuilder("kick").plugin(this).build(), new Kick());
        logger.info("Registered Velocity command /kick");
        commandManager.register(commandManager.metaBuilder("mute").plugin(this).build(), new Mute());
        logger.info("Registered Velocity command /mute");
        commandManager.register(commandManager.metaBuilder("sim").plugin(this).build(), new Sim());
        logger.info("Registered Velocity command /sim");
        commandManager.register(commandManager.metaBuilder("warn").plugin(this).build(), new Warn());
        logger.info("Registered Velocity command /warn");
        commandManager.register(commandManager.metaBuilder("unban").plugin(this).build(), new Unban());
        logger.info("Registered Velocity command /unban");
    }
}
