package org.tavall.minecraft.bootstrap;

import org.tavall.api.minecraft.cache.KingdomCacheConfig;
import org.tavall.api.minecraft.cache.IKingdomSemanticCacheGateway;
import org.tavall.api.minecraft.cache.KingdomSemanticCacheGateway;
import org.tavall.api.minecraft.backend.rank.RankAccess;
import org.tavall.minecraft.commands.IGuild;
import org.tavall.minecraft.commands.IRank;
import org.tavall.minecraft.commands.ISim;
import org.tavall.minecraft.bootstrap.VelocityDependencies;
import org.tavall.api.minecraft.guild.IGuildCreationHandler;
import org.tavall.api.minecraft.guild.IGuildStateStore;
import org.tavall.dependency.composition.IDependencyBundleAccess;
import org.tavall.minecraft.commands.support.VelocityProxyFixtures;
import org.tavall.dependency.DependencyLoader;
import org.tavall.dependency.DependencyLoaderAccess;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.proxy.ProxyServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class VelocityDependencyModuleIntegrationTest {
    @BeforeEach
    void clearDependencies() {
        DependencyLoader.getDependencyLoader().clear();
        DependencyLoaderAccess.clear();
    }

    @AfterEach
    void clearDependenciesAfterTest() {
        DependencyLoader.getDependencyLoader().clear();
        DependencyLoaderAccess.clear();
    }

    @Test
    void minecraftProxyConfigReadsAndNormalizesEnvironmentValues() {
        VelocityProxyConfig config = VelocityProxyConfig.fromEnvironment(Map.of(
                "RESOURCE_GAME_MINECRAFT_COMMAND_PERMISSION", "tavall.resourcegame.command",
                "RESOURCE_GAME_MINECRAFT_ADMIN_PERMISSION", "tavall.resourcegame.admin",
                "RESOURCE_GAME_MINECRAFT_SERVER_ID", "velocity-test",
                "RESOURCE_GAME_MINECRAFT_INSTANCE_SERVER_MAP", "mine-1 = alpha , mine-2= beta",
                "RESOURCE_GAME_MINECRAFT_OWNER_USERNAMES", "Alice, BOB ",
                "RESOURCE_GAME_MINECRAFT_ADMIN_USERNAMES", "Carol",
                "RESOURCE_GAME_MINECRAFT_CONSOLE_ADMIN", "false"
        ));

        assertEquals("tavall.resourcegame.command", config.commandPermission());
        assertEquals("tavall.resourcegame.admin", config.adminPermission());
        assertEquals("velocity-test", config.serverId());
        assertEquals(Map.of("mine-1", "alpha", "mine-2", "beta"), config.instanceServerMappings());
        assertEquals(java.util.Set.of("alice", "bob"), config.ownerUsernames());
        assertEquals(java.util.Set.of("carol"), config.adminUsernames());
        assertFalse(config.allowConsoleAdmin());
    }

    @Test
    void minecraftProxyConfigFallsBackToRepoDefaultsWhenEnvironmentIsEmpty() {
        VelocityProxyConfig config = VelocityProxyConfig.fromEnvironment(Map.of());

        assertEquals("tavall.resourcegame.command", config.commandPermission());
        assertEquals("tavall.resourcegame.admin", config.adminPermission());
        assertEquals("velocity-proxy", config.serverId());
        assertEquals(Map.of(), config.instanceServerMappings());
        assertEquals(java.util.Set.of(), config.ownerUsernames());
        assertEquals(java.util.Set.of(), config.adminUsernames());
        assertTrue(config.allowConsoleAdmin());
    }

    @Test
    void dependencyModuleRegistersSharedCollaboratorsWithoutBridgeTokens() {
        VelocityProxyConfig config = new VelocityProxyConfig(
                "tavall.resourcegame.command",
                "tavall.resourcegame.admin",
                "velocity-test",
                Map.of(),
                java.util.Set.of(),
                java.util.Set.of(),
                true
        );

        new VelocityDependencyModule().registerDependencies(config);

        assertSame(config, DependencyLoaderAccess.findInstance(VelocityProxyConfig.class));
        assertInstanceOf(KingdomCacheConfig.class, DependencyLoaderAccess.findInstance(KingdomCacheConfig.class));
        assertInstanceOf(KingdomSemanticCacheGateway.class, DependencyLoaderAccess.findInstance(IKingdomSemanticCacheGateway.class));
        assertInstanceOf(VelocityDependencies.class, DependencyLoaderAccess.findInstance(VelocityDependencies.class));
        assertInstanceOf(IGuildStateStore.class, DependencyLoaderAccess.findInstance(IGuildStateStore.class));
        assertInstanceOf(IGuildCreationHandler.class, DependencyLoaderAccess.findInstance(IGuildCreationHandler.class));
        assertInstanceOf(IGuild.class, DependencyLoaderAccess.findInstance(IGuild.class));
        assertInstanceOf(ISim.class, DependencyLoaderAccess.findInstance(ISim.class));
        assertInstanceOf(IRank.class, DependencyLoaderAccess.findInstance(IRank.class));
        assertInstanceOf(RankAccess.class, DependencyLoaderAccess.findInstance(RankAccess.class));
        assertInstanceOf(org.tavall.minecraft.permissions.VelocityPermissionResolver.class, DependencyLoaderAccess.findInstance(org.tavall.minecraft.permissions.IVelocityPermissionResolver.class));
        assertInstanceOf(org.tavall.minecraft.permissions.VelocityCommandPermissionHandler.class, DependencyLoaderAccess.findInstance(org.tavall.minecraft.permissions.IVelocityCommandPermissionHandler.class));

        VelocityDependencies velocityDependencies = DependencyLoaderAccess.findInstance(VelocityDependencies.class);
        assertSame(config, velocityDependencies.velocityProxyConfig());
        assertInstanceOf(IGuild.class, velocityDependencies.guildCommand());
        assertInstanceOf(ISim.class, velocityDependencies.simCommand());
        assertInstanceOf(IRank.class, velocityDependencies.rankCommand());
        assertInstanceOf(RankAccess.class, velocityDependencies.rankAccess());

        IDependencyBundleAccess<?> guildBundleAccess = (IDependencyBundleAccess<?>) DependencyLoaderAccess.requireInstance(IGuildCreationHandler.class);
        IDependencyBundleAccess<?> commandBundleAccess = (IDependencyBundleAccess<?>) DependencyLoaderAccess.requireInstance(IGuild.class);
        Object guildDependencies = guildBundleAccess.getDependencies();
        Object commandDependencies = commandBundleAccess.getDependencies();
        assertEquals(guildDependencies, commandDependencies);
    }

    @Test
    void velocityProxyPluginDelegatesInitializationThroughBootstrap() {
        List<String> registrations = new ArrayList<>();
        CommandManager commandManager = VelocityProxyFixtures.recordingCommandManager(registrations);
        ProxyServer proxyServer = VelocityProxyFixtures.proxyServer(commandManager, Map.of());
        org.slf4j.Logger logger = VelocityProxyFixtures.logger();

        new VelocityProxyPlugin(proxyServer, logger).onProxyInitialization(null);

        assertSame(proxyServer, DependencyLoaderAccess.findInstance(ProxyServer.class));
        assertTrue(registrations.contains("sim:Sim"));
        assertTrue(registrations.contains("guild:BrigadierCommand"));
    }
}



