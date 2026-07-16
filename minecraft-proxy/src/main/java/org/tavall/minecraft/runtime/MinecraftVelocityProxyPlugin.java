package org.tavall.minecraft.runtime;

import com.google.inject.Inject;
import org.tavall.minecraft.runtime.bootstrap.MinecraftVelocityBootstrap;
import org.tavall.minecraft.switching.ProxyServerSwitchGateway;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

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
        new MinecraftVelocityBootstrap(proxyServer, logger, config, new ProxyServerSwitchGateway()).initialize();
    }
}
