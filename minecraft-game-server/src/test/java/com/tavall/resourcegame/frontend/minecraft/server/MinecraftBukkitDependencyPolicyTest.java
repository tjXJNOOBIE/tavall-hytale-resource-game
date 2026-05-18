package org.tavall.minecraft.server;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class MinecraftBukkitDependencyPolicyTest {
    @Test
    void bukkitSurfaceHandlersUseGeneratedDefaultDependenciesInsteadOfConstructorGraphWiring() throws IOException {
        Pattern constructorDependency = Pattern.compile("public\\s+MinecraftBukkit[A-Za-z0-9]+\\s*\\([^)]*[A-Z][A-Za-z0-9_<>?, ]+\\s+[a-z]");
        Pattern privateDependencyField = Pattern.compile("private\\s+final\\s+(MinecraftBukkit|Logger|URI|ObjectMapper).*;");

        try (var paths = Files.walk(Path.of("src/main/java/com/tavall/resourcegame/frontend/minecraft/server"))) {
            List<Path> files = paths
                    .filter(path -> {
                        String name = path.getFileName().toString();
                        return name.endsWith("Handler.java")
                                || name.endsWith("Command.java")
                                || name.endsWith("Router.java")
                                || name.endsWith("Plugin.java");
                    })
                    .toList();

            for (Path file : files) {
                String source = Files.readString(file);
                assertFalse(source.contains("@Inject"), file + " should not use framework constructor injection.");
                assertFalse(source.contains("@Autowired"), file + " should not use Spring field or constructor injection.");
                assertFalse(constructorDependency.matcher(source).find(), file + " should not constructor-wire Bukkit surface dependencies.");
                assertFalse(privateDependencyField.matcher(source).find(), file + " should not cache Bukkit surface dependencies in fields.");
            }
        }
    }

    @Test
    void bukkitEntrypointAdaptsFrameworkObjectsBehindMinecraftInterfaces() throws IOException {
        String pluginSource = Files.readString(Path.of("src/main/java/com/tavall/resourcegame/frontend/minecraft/server/MinecraftBukkitServerPlugin.java"));
        String bootstrapSource = Files.readString(Path.of("src/main/java/com/tavall/resourcegame/frontend/minecraft/server/bootstrap/MinecraftBukkitBootstrap.java"));
        String commandClientSource = Files.readString(Path.of("src/main/java/com/tavall/resourcegame/frontend/minecraft/server/MinecraftBukkitCommandClientHandler.java"));
        String dependencyModuleSource = Files.readString(Path.of("src/main/java/com/tavall/resourcegame/frontend/minecraft/server/MinecraftBukkitServerDependencyModule.java"));
        String accountGuiSource = Files.readString(Path.of("src/main/java/com/tavall/resourcegame/frontend/minecraft/server/KingdomAccountGui.java"));
        String controlServerDependencyModuleSource = Files.readString(Path.of("..", "control-server", "src", "main", "java", "com", "tavall", "resourcegame", "controlserver", "ControlServerDependencyModule.java"));
        String playerDataApiSource = Files.readString(Path.of("..", "control-server", "src", "main", "java", "com", "tavall", "resourcegame", "controlserver", "api", "PlayerDataApi.java"));
        String controlBridgeServerSource = Files.readString(Path.of("..", "control-server", "src", "main", "java", "com", "tavall", "resourcegame", "controlserver", "transport", "ControlPlaneTcpBridgeServer.java"));

        assertTrue(pluginSource.contains("bootstrap = new MinecraftBukkitBootstrap(this);"));
        assertTrue(pluginSource.contains("bootstrap.initialize();"));
        assertTrue(pluginSource.contains("bootstrap.shutdown();"));
        assertFalse(pluginSource.contains("DependencyLoaderAccess.registerInstance(IMinecraftBukkitServerConfig.class"));
        assertFalse(pluginSource.contains("DependencyLoaderAccess.registerInstance(IMinecraftBukkitRuntimeState.class"));
        assertFalse(pluginSource.contains("registerEvents(getMinecraftBukkitPlayerJoinHandler(), this)"));
        assertFalse(pluginSource.contains("registerEvents(getMinecraftBukkitInteractionHandler(), this)"));
        assertFalse(pluginSource.contains("registerEvents(getKingdomInventoryUiHandler(), this)"));
        assertFalse(pluginSource.contains("command.setExecutor(getMinecraftBukkitCommandHandler())"));
        assertFalse(pluginSource.contains("registerEvents(this, this)"));
        assertFalse(pluginSource.contains("registerInstance(Server.class"));
        assertFalse(pluginSource.contains("registerInstance(JavaPlugin.class"));
        assertFalse(pluginSource.contains("registerInstance(PluginCommand.class"));
        assertTrue(bootstrapSource.contains("DependencyLoaderAccess.registerInstance(IMinecraftBukkitServerConfig.class"));
        assertTrue(bootstrapSource.contains("DependencyLoaderAccess.registerInstance(IMinecraftBukkitRuntimeState.class"));
        assertTrue(bootstrapSource.contains("DependencyLoaderAccess.registerInstance(MinecraftBukkitServerView.class, new BukkitServerViewAdapter(plugin.getServer()))"));
        assertTrue(bootstrapSource.contains("DependencyLoaderAccess.registerInstance(IMinecraftBukkitLogger.class"));
        assertTrue(bootstrapSource.contains("registerEvents(plugin.getMinecraftBukkitPlayerJoinHandler(), plugin)"));
        assertTrue(bootstrapSource.contains("registerEvents(plugin.getMinecraftBukkitInteractionHandler(), plugin)"));
        assertTrue(bootstrapSource.contains("registerEvents(plugin.getKingdomInventoryUiHandler(), plugin)"));
        assertTrue(bootstrapSource.contains("command.setExecutor(plugin.getMinecraftBukkitCommandHandler())"));
        assertFalse(commandClientSource.contains("ControlCommandRuntime"));
        assertFalse(commandClientSource.contains("getMinecraftBukkitDirectControlRuntimeHandler()"));
        assertTrue(commandClientSource.contains("fetchPlayerData("));
        assertFalse(dependencyModuleSource.contains("IMinecraftBukkitDirectControlRuntimeHandler"));
        assertTrue(dependencyModuleSource.contains("FrontendTcpControlCommandClient"));
        assertTrue(dependencyModuleSource.contains("tcp://127.0.0.1:18081"));
        assertFalse(dependencyModuleSource.contains("Frontend" + "HttpControlCommandClient"));
        assertTrue(accountGuiSource.contains("fetchPlayerData("));
        assertTrue(accountGuiSource.contains("new PlayerDataRequest("));
        assertTrue(accountGuiSource.contains("getMinecraftBukkitInteractionMenuHandler().open(player, toMenu(response), response.message())"));
        assertFalse(accountGuiSource.contains("accountRepository().saveAccount"));
        assertFalse(accountGuiSource.contains("platformAccountBindingRepository().savePlatformBinding"));
        assertTrue(controlServerDependencyModuleSource.contains("PlayerDataApi.class"));
        assertTrue(playerDataApiSource.contains("package org.tavall.control.api;"));
        assertTrue(playerDataApiSource.contains("runtime.accountRepository()"));
        assertTrue(playerDataApiSource.contains("runtime.platformAccountBindingRepository()"));
        assertTrue(controlBridgeServerSource.contains("PLAYER_DATA_REQUEST"));
    }

    @Test
    void bukkitRawEventsUseDedicatedHandlersWithStableMonitorOrdering() throws IOException {
        String interactSource = Files.readString(Path.of("src/main/java/com/tavall/resourcegame/frontend/minecraft/server/MinecraftBukkitInteractionHandler.java"));
        String joinSource = Files.readString(Path.of("src/main/java/com/tavall/resourcegame/frontend/minecraft/server/MinecraftBukkitPlayerJoinHandler.java"));

        assertTrue(interactSource.contains("@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)"));
        assertTrue(joinSource.contains("@EventHandler(priority = EventPriority.MONITOR)"));
        assertTrue(joinSource.contains("getMinecraftBukkitTaskScheduler().runAsync(getMinecraftBukkitSnapshotSubmitHandler()::submitSnapshotQuietly)"));
    }

    @Test
    void bukkitCommandExposurePublishesServerSurfaceDiagnosticsCommand() throws IOException {
        String pluginDescriptor = Files.readString(Path.of("src/main/resources/plugin.yml"));

        assertTrue(pluginDescriptor.contains("main: org.tavall.minecraft.server.MinecraftBukkitServerPlugin"));
        assertTrue(pluginDescriptor.contains("commands:"));
        assertTrue(pluginDescriptor.contains("  tavallserver:"));
        assertTrue(pluginDescriptor.contains("usage: /tavallserver [snapshot|visual <chat|title> <message>|interact <message>]"));
        assertTrue(pluginDescriptor.contains("  kd:"));
        assertTrue(pluginDescriptor.contains("usage: /kd [help|ui|data|castle|citizens|troops|resources|account|buildings|building|npc|nodes|place|interior|hologram|entity|scene|bootstrap|tick|debug|companion...]"));
        assertTrue(pluginDescriptor.contains("      - kingdom"));
        assertTrue(pluginDescriptor.contains("tavall.resourcegame.server:"));
        assertTrue(pluginDescriptor.contains("tavall.resourcegame.command:"));
        assertTrue(pluginDescriptor.contains("default: op"));
    }

    @Test
    void bukkitServerFrontendDoesNotIntroduceServiceClasses() throws IOException {
        try (var paths = Files.walk(Path.of("src/main/java"))) {
            List<Path> serviceClasses = paths
                    .filter(path -> path.getFileName().toString().endsWith("Service.java"))
                    .toList();

            assertTrue(serviceClasses.isEmpty(), "Bukkit server frontend should keep Handler naming: " + serviceClasses);
        }
    }
}
