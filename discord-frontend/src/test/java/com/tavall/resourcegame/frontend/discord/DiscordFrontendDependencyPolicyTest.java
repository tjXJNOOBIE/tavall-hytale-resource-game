package com.tavall.resourcegame.frontend.discord;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class DiscordFrontendDependencyPolicyTest {
    @Test
    void discordSurfaceRegistersOnlyDiscordInterfaceTokens() throws IOException {
        String source = Files.readString(Path.of("src/main/java/com/tavall/resourcegame/frontend/discord/DiscordFrontendDependencyModule.java"));

        assertTrue(source.contains("registerIfMissing(IDiscordBotConfig.class"));
        assertTrue(source.contains("registerIfMissing(IDiscordFrontendModule.class"));
        assertTrue(source.contains("registerIfMissing(IDiscordFrontendCommandEnvelopeFactory.class"));
        assertTrue(source.contains("registerIfMissing(IDiscordKdCommandInputFormatterHandler.class"));
        assertTrue(source.contains("registerIfMissing(IDiscordKdCommandEnvelopeBridge.class"));
        assertTrue(source.contains("registerIfMissing(IDiscordControlCommandClient.class"));
        assertTrue(source.contains("registerIfMissing(IDiscordControlPlaneCommandBridge.class"));
        assertTrue(source.contains("registerIfMissing(IDiscordInteractionCommandHandler.class"));
        assertTrue(source.contains("registerIfMissing(IDiscordSlashCommandRegistrar.class"));
        assertFalse(source.contains("Minecraft"));
        assertFalse(source.contains("Velocity"));
        assertFalse(source.contains("Bukkit"));
        assertFalse(source.contains("Roblox"));
    }

    @Test
    void discordApplicationLogicUsesGeneratedDefaultDependencies() throws IOException {
        List<Path> files = List.of(
                Path.of("src/main/java/com/tavall/resourcegame/frontend/discord/DiscordCommandPermissionHandler.java"),
                Path.of("src/main/java/com/tavall/resourcegame/frontend/discord/DiscordControlPlaneCommandBridge.java"),
                Path.of("src/main/java/com/tavall/resourcegame/frontend/discord/DiscordFrontendCommandEnvelopeFactory.java"),
                Path.of("src/main/java/com/tavall/resourcegame/frontend/discord/DiscordInteractionCommandHandler.java"),
                Path.of("src/main/java/com/tavall/resourcegame/frontend/discord/DiscordKdCommandEnvelopeBridge.java"),
                Path.of("src/main/java/com/tavall/resourcegame/frontend/discord/DiscordKdCommandInputFormatterHandler.java"),
                Path.of("src/main/java/com/tavall/resourcegame/frontend/discord/DiscordPermissionResolver.java"),
                Path.of("src/main/java/com/tavall/resourcegame/frontend/discord/DiscordSlashCommandRegistrar.java")
        );
        Pattern injectedConstructor = Pattern.compile("public\\s+Discord[A-Za-z0-9]+\\s*\\([^)]*[A-Z][A-Za-z0-9_<>?, ]+\\s+[a-z]");
        Pattern cachedDependencyField = Pattern.compile("private\\s+final\\s+(I?Discord|JDA|HttpClient|ObjectMapper).*;");

        for (Path file : files) {
            String source = Files.readString(file);
            assertFalse(source.contains("@Inject"), file + " should not use framework constructor injection.");
            assertFalse(source.contains("@Autowired"), file + " should not use Spring field or constructor injection.");
            assertFalse(injectedConstructor.matcher(source).find(), file + " should not constructor-wire app dependencies.");
            assertFalse(cachedDependencyField.matcher(source).find(), file + " should not cache app dependencies in fields.");
        }
    }

    @Test
    void discordEntrypointUsesDomainAccessorsAfterLifecycleRegistration() throws IOException {
        String source = Files.readString(Path.of("src/main/java/com/tavall/resourcegame/frontend/discord/ResourceGameDiscordBotApplication.java"));

        assertTrue(source.contains("implements IDiscordFrontendDomain"));
        assertTrue(source.contains("new DiscordFrontendDependencyModule().registerDependencies(config)"));
        assertTrue(source.contains("getDiscordBotConfig()"));
        assertTrue(source.contains("getDiscordInteractionCommandHandler()"));
        assertTrue(source.contains("getDiscordSlashCommandRegistrar().registerCommands"));
        assertFalse(source.contains("new DiscordInteractionCommandHandler()"));
        assertFalse(source.contains("new DiscordSlashCommandRegistrar()"));
        assertFalse(source.contains("registerInstance(JDA.class"));
    }

    @Test
    void discordDomainDefaultMethodsExposeDiscordRuntimeTokens() throws IOException {
        String source = Files.readString(Path.of("src/main/java/com/tavall/resourcegame/frontend/discord/IDiscordFrontendDomainGenerated.java"));

        assertTrue(source.contains("DependencyLoaderAccess.requireInstance(IDiscordBotConfig.class)"));
        assertTrue(source.contains("DependencyLoaderAccess.requireInstance(IDiscordInteractionCommandHandler.class)"));
        assertTrue(source.contains("DependencyLoaderAccess.requireInstance(IDiscordSlashCommandRegistrar.class)"));
        assertFalse(source.contains("Minecraft"));
        assertFalse(source.contains("Bukkit"));
        assertFalse(source.contains("Roblox"));
    }

    @Test
    void discordFrontendDoesNotIntroduceServiceClasses() throws IOException {
        try (var paths = Files.walk(Path.of("src/main/java"))) {
            List<Path> serviceClasses = paths
                    .filter(path -> path.getFileName().toString().endsWith("Service.java"))
                    .toList();

            assertTrue(serviceClasses.isEmpty(), "Discord frontend should keep Handler naming: " + serviceClasses);
        }
    }
}
