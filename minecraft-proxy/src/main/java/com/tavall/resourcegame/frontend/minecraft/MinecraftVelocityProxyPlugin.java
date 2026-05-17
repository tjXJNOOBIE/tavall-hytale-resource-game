package com.tavall.resourcegame.frontend.minecraft;

import com.google.inject.Inject;
import com.tavall.resourcegame.frontend.minecraft.commands.Ban;
import com.tavall.resourcegame.frontend.minecraft.commands.Kick;
import com.tavall.resourcegame.frontend.minecraft.commands.MinecraftVelocityRankCommand;
import com.tavall.resourcegame.frontend.minecraft.commands.Mute;
import com.tavall.resourcegame.frontend.minecraft.commands.Sim;
import com.tavall.resourcegame.frontend.minecraft.commands.Unban;
import com.tavall.resourcegame.frontend.minecraft.commands.Warn;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.slf4j.Logger;

import java.util.Map;

@Plugin(
        id = "tavall-resource-game",
        name = "Tavall Resource Game",
        version = "0.1.0-SNAPSHOT",
        description = "Velocity proxy adapter for Tavall Resource Game control-plane commands.",
        authors = {"Tavall"}
)
public final class MinecraftVelocityProxyPlugin {
    private final ProxyServer proxyServer;
    private final Logger logger;

    @Inject
    public MinecraftVelocityProxyPlugin(ProxyServer proxyServer, Logger logger) {
        this.proxyServer = proxyServer;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        MinecraftProxyConfig config = MinecraftProxyConfig.fromEnvironment(System.getenv());
        if (!DependencyLoaderAccess.isInstanceRegistered(IMinecraftVelocityProxyServer.class)) {
            DependencyLoaderAccess.registerInstance(IMinecraftVelocityProxyServer.class, new MinecraftVelocityProxyServerAdapter(proxyServer));
        }
        new MinecraftFrontendDependencyModule().registerDependencies(config, new ProxyServerSwitchGateway());
        proxyServer.getEventManager().register(this, new MinecraftVelocityLoginEvent());
        proxyServer.getEventManager().register(this, new MinecraftVelocityMuteChatEvent());
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
        logger.info("Registered Tavall Resource Game Velocity routing adapter. serverId={}", config.serverId());
    }

    Map<String, String> environmentForTests() {
        return System.getenv();
    }
}
