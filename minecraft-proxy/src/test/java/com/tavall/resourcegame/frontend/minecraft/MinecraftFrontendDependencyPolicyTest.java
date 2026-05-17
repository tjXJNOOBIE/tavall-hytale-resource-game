package com.tavall.resourcegame.frontend.minecraft;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class MinecraftFrontendDependencyPolicyTest {
    @Test
    void velocityEntrypointAdaptsProxyServerBehindMinecraftInterfaceToken() throws IOException {
        String source = Files.readString(Path.of("src/main/java/com/tavall/resourcegame/frontend/minecraft/runtime/MinecraftVelocityProxyPlugin.java"));

        assertTrue(source.contains("@Inject"));
        assertTrue(source.contains("new MinecraftVelocityProxyServerAdapter(proxyServer)"));
        assertTrue(source.contains("IMinecraftVelocityProxyServer.class"));
        assertTrue(source.contains("new MinecraftFrontendDependencyModule().registerDependencies(config, new ProxyServerSwitchGateway())"));
        assertTrue(source.contains("metaBuilder(\"rank\")"));
        assertTrue(source.contains("new MinecraftVelocityRankCommand()"));
        assertTrue(source.contains("new Kick()"));
        assertTrue(source.contains("new Mute()"));
        assertTrue(source.contains("new Sim()"));
        assertFalse(source.contains("registerInstance(ProxyServer.class"));
        assertFalse(source.contains("registerInstance(Logger.class"));
    }

    @Test
    void velocityDoesNotExposeKingdomCommandsOnProxy() throws IOException {
        String pluginDescriptor = Files.readString(Path.of("src/main/resources/velocity-plugin.json"));
        String pluginSource = Files.readString(Path.of("src/main/java/com/tavall/resourcegame/frontend/minecraft/runtime/MinecraftVelocityProxyPlugin.java"));
        String commandSource = Files.readString(Path.of("src/main/java/com/tavall/resourcegame/frontend/minecraft/commands/MinecraftVelocitySimpleCommand.java"));

        assertTrue(pluginDescriptor.contains("\"main\": \"com.tavall.resourcegame.frontend.minecraft.runtime.MinecraftVelocityProxyPlugin\""));
        assertTrue(pluginSource.contains("metaBuilder(\"rank\")"));
        assertFalse(pluginSource.contains("metaBuilder(\"kd\")"));
        assertFalse(pluginSource.contains("aliases(\"kingdom\")"));
        assertFalse(pluginSource.contains("new MinecraftVelocitySimpleCommand()"));
        assertTrue(pluginSource.contains("new MinecraftVelocityRankCommand()"));
        assertTrue(pluginSource.contains("new Ban()"));
        assertTrue(pluginSource.contains("new Kick()"));
        assertTrue(pluginSource.contains("new Mute()"));
        assertTrue(pluginSource.contains("new Sim()"));
        assertTrue(pluginSource.contains("new Warn()"));
        assertTrue(pluginSource.contains("new Unban()"));
        assertTrue(pluginSource.contains("new MinecraftVelocityLoginEvent()"));
        assertTrue(pluginSource.contains("new MinecraftVelocityMuteChatEvent()"));
        assertTrue(commandSource.contains("getMinecraftVelocityCommandExecutionHandler().execute("));
        assertTrue(commandSource.contains("getMinecraftVelocityCommandPermissionHandler().canExecute("));
        assertFalse(pluginSource.contains("new MinecraftVelocityCommandExecutionHandler()"));
    }

    @Test
    void velocityApplicationLogicUsesGeneratedDefaultDependencies() throws IOException {
        List<Path> files = List.of(
                Path.of("src/main/java/com/tavall/resourcegame/frontend/minecraft/bridge/MinecraftControlPlaneCommandBridge.java"),
                Path.of("src/main/java/com/tavall/resourcegame/frontend/minecraft/bridge/MinecraftDirectControlCommandClient.java"),
                Path.of("src/main/java/com/tavall/resourcegame/frontend/minecraft/commands/MinecraftVelocityRankCommand.java"),
                Path.of("src/main/java/com/tavall/resourcegame/frontend/minecraft/bridge/MinecraftKdCommandEnvelopeBridge.java"),
                Path.of("src/main/java/com/tavall/resourcegame/frontend/minecraft/routing/MinecraftVelocityCommandExecutionHandler.java"),
                Path.of("src/main/java/com/tavall/resourcegame/frontend/minecraft/permissions/MinecraftVelocityCommandPermissionHandler.java"),
                Path.of("src/main/java/com/tavall/resourcegame/frontend/minecraft/switching/MinecraftVelocityInstanceSwitchHandler.java"),
                Path.of("src/main/java/com/tavall/resourcegame/frontend/minecraft/permissions/MinecraftVelocityPermissionResolver.java"),
                Path.of("src/main/java/com/tavall/resourcegame/frontend/minecraft/switching/ProxyServerSwitchGateway.java")
        );
        Pattern injectedConstructor = Pattern.compile("public\\s+[A-Za-z0-9]+\\s*\\([^)]*[A-Z][A-Za-z0-9_<>?, ]+\\s+[a-z]");
        Pattern cachedDependencyField = Pattern.compile("private\\s+final\\s+(I?Minecraft|ProxyServer|Logger|HttpClient|ObjectMapper).*;");

        for (Path file : files) {
            String source = Files.readString(file);
            assertFalse(source.contains("@Inject"), file + " should not use framework constructor injection.");
            assertFalse(source.contains("@Autowired"), file + " should not use Spring field or constructor injection.");
            assertFalse(source.contains("ControlCommandRuntime"), file + " should not use in-process control runtime.");
            assertFalse(source.contains("ControlCommandRuntimeFactory"), file + " should not recreate the control runtime locally.");
            assertFalse(injectedConstructor.matcher(source).find(), file + " should not constructor-wire app dependencies.");
            assertFalse(cachedDependencyField.matcher(source).find(), file + " should not cache app dependencies in fields.");
        }
    }

    @Test
    void velocityDependencyModuleUsesTcpControlBridgeClient() throws IOException {
        String moduleSource = Files.readString(Path.of("src/main/java/com/tavall/resourcegame/frontend/minecraft/runtime/MinecraftFrontendDependencyModule.java"));

        assertTrue(moduleSource.contains("FrontendTcpControlCommandClient"));
        assertFalse(moduleSource.contains("Frontend" + "HttpControlCommandClient"));
        assertTrue(moduleSource.contains("tcp://127.0.0.1:18081"));
        assertTrue(moduleSource.contains("IMinecraftControlCommandClient.class"));
        assertFalse(moduleSource.contains("selectControlCommandClient"));
    }

    @Test
    void minecraftFrontendDoesNotIntroduceServiceClasses() throws IOException {
        try (var paths = Files.walk(Path.of("src/main/java"))) {
            List<Path> serviceClasses = paths
                    .filter(path -> path.getFileName().toString().endsWith("Service.java"))
                    .toList();

            assertTrue(serviceClasses.isEmpty(), "Minecraft frontend should keep Handler naming: " + serviceClasses);
        }
    }
}
