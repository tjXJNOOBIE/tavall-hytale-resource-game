package com.tavall.hytale.resourcegame.frontend.minecraft;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
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
        MinecraftControlPlaneCommandBridge commandBridge = new MinecraftControlPlaneCommandBridge(
                new MinecraftKdCommandEnvelopeBridge(),
                new MinecraftHttpControlCommandClient(config.controlIngressUri())
        );
        MinecraftVelocityCommandPermissionHandler permissionHandler = new MinecraftVelocityCommandPermissionHandler(
                config.commandPermission(),
                config.adminPermission(),
                new MinecraftVelocityPermissionResolver(
                        config.commandPermission(),
                        config.adminPermission(),
                        config.ownerUsernames(),
                        config.adminUsernames(),
                        config.allowConsoleAdmin()
                ),
                new com.tavall.hytale.resourcegame.shared.permissions.UniversalPermissionPolicy()
        );
        MinecraftVelocityCommandExecutionHandler executionHandler = new MinecraftVelocityCommandExecutionHandler(
                commandBridge,
                permissionHandler,
                config.serverId()
        );
        MinecraftVelocitySimpleCommand command = new MinecraftVelocitySimpleCommand(executionHandler, permissionHandler);
        proxyServer.getCommandManager().register(
                proxyServer.getCommandManager().metaBuilder("kd").aliases("kingdom").plugin(this).build(),
                command
        );
        logger.info("Registered Tavall Resource Game Velocity control-plane command adapter. ingress={}", config.controlIngressUri());
    }

    Map<String, String> environmentForTests() {
        return System.getenv();
    }
}
