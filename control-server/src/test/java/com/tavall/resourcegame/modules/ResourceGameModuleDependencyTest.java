package com.tavall.resourcegame.modules;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ResourceGameModuleDependencyTest {
    private static final Path RESOURCE_GAME_ROOT = resolveResourceGameRoot();
    private static final Pattern VENDORED_ABSTRACT_CACHE_PACKAGE = Pattern.compile(
            "(?m)^\\s*package\\s+org\\.tavall\\.abstractcache"
    );

    @Test
    void minecraftMainFrontendModulesUseSharedContractsAndTavallToolsThroughMavenDependencies() throws IOException {
        String minecraftProxyPom = Files.readString(RESOURCE_GAME_ROOT.resolve("minecraft-proxy").resolve("pom.xml"));
        String minecraftGameServerPom = Files.readString(RESOURCE_GAME_ROOT.resolve("minecraft-game-server").resolve("pom.xml"));

        assertTrue(minecraftProxyPom.contains("<artifactId>game-api</artifactId>"));
        assertTrue(minecraftProxyPom.contains("<artifactId>tavall-logging</artifactId>"));
        assertTrue(minecraftProxyPom.contains("<artifactId>tavall-di</artifactId>"));

        assertTrue(minecraftGameServerPom.contains("<artifactId>game-api</artifactId>"));
        assertTrue(minecraftGameServerPom.contains("<artifactId>tavall-di</artifactId>"));
    }

    @Test
    void controlServerUsesTavallToolingAndCacheThroughMavenDependencies() throws IOException {
        String pom = Files.readString(RESOURCE_GAME_ROOT.resolve("control-server").resolve("pom.xml"));

        assertTrue(pom.contains("<artifactId>tavall-logging</artifactId>"));
        assertTrue(pom.contains("<artifactId>tavall-di</artifactId>"));
        assertTrue(pom.contains("<artifactId>tavall-eventbus</artifactId>"));
        assertTrue(pom.contains("<artifactId>tavall-concurrency</artifactId>"));
        assertTrue(pom.contains("<artifactId>tavall-scheduler</artifactId>"));
        assertTrue(pom.contains("<artifactId>abstract-cache-semantic</artifactId>"));
        assertTrue(pom.contains("<artifactId>abstract-cache-storage-memory</artifactId>"));
    }

    @Test
    void minecraftMainPrunesLegacyPlatformAndSplitBackendModulesFromAggregator() throws IOException {
        String rootPom = Files.readString(RESOURCE_GAME_ROOT.resolve("pom.xml"));

        assertTrue(rootPom.contains("<module>control-server</module>"));
        assertTrue(rootPom.contains("<module>minecraft-proxy</module>"));
        assertTrue(rootPom.contains("<module>minecraft-game-server</module>"));
        assertTrue(!rootPom.contains("<module>core</module>"));
        assertTrue(!rootPom.contains("<module>cloud-core</module>"));
        assertTrue(!rootPom.contains("<module>cloud-agent</module>"));
        assertTrue(!rootPom.contains("<module>cloud-control-plane</module>"));
        assertTrue(!rootPom.contains("<module>distribution</module>"));
        assertTrue(!rootPom.contains("<module>events</module>"));
        assertTrue(!rootPom.contains("<module>liveops</module>"));
        assertTrue(!rootPom.contains("<module>hytale-frontend</module>"));
        assertTrue(!rootPom.contains("<module>roblox-frontend</module>"));
        assertTrue(!rootPom.contains("<module>pc-app</module>"));
        assertTrue(!rootPom.contains("<module>android-app</module>"));
        assertTrue(!rootPom.contains("<module>discord-frontend</module>"));
        assertTrue(!Files.exists(RESOURCE_GAME_ROOT.resolve("core")));
        assertTrue(!Files.exists(RESOURCE_GAME_ROOT.resolve("cloud-core")));
        assertTrue(!Files.exists(RESOURCE_GAME_ROOT.resolve("cloud-agent")));
        assertTrue(!Files.exists(RESOURCE_GAME_ROOT.resolve("cloud-control-plane")));
        assertTrue(!Files.exists(RESOURCE_GAME_ROOT.resolve("distribution")));
        assertTrue(!Files.exists(RESOURCE_GAME_ROOT.resolve("events")));
        assertTrue(!Files.exists(RESOURCE_GAME_ROOT.resolve("liveops")));
        assertTrue(!Files.exists(RESOURCE_GAME_ROOT.resolve("hytale-frontend")));
        assertTrue(!Files.exists(RESOURCE_GAME_ROOT.resolve("roblox-frontend")));
        assertTrue(!Files.exists(RESOURCE_GAME_ROOT.resolve("pc-app")));
        assertTrue(!Files.exists(RESOURCE_GAME_ROOT.resolve("android-app")));
        assertTrue(!Files.exists(RESOURCE_GAME_ROOT.resolve("discord-frontend")));
    }

    @Test
    void controlServerOwnsConsolidatedBackendPackagesWithoutLegacyModuleDependencies() throws IOException {
        String controlServerPom = Files.readString(RESOURCE_GAME_ROOT.resolve("control-server").resolve("pom.xml"));
        Path controlServerMainPackage = RESOURCE_GAME_ROOT
                .resolve("control-server")
                .resolve("src/main/java/com/tavall/resourcegame");

        assertTrue(!controlServerPom.contains("<artifactId>core</artifactId>"));
        assertTrue(!controlServerPom.contains("<artifactId>cloud-core</artifactId>"));
        assertTrue(!controlServerPom.contains("<artifactId>cloud-agent</artifactId>"));
        assertTrue(!controlServerPom.contains("<artifactId>cloud-control-plane</artifactId>"));
        assertTrue(!controlServerPom.contains("<artifactId>distribution</artifactId>"));
        assertTrue(!controlServerPom.contains("<artifactId>events</artifactId>"));
        assertTrue(!controlServerPom.contains("<artifactId>liveops</artifactId>"));
        assertTrue(Files.exists(controlServerMainPackage.resolve("distribution")));
        assertTrue(Files.exists(controlServerMainPackage.resolve("events")));
        assertTrue(Files.exists(controlServerMainPackage.resolve("liveops")));
        assertTrue(Files.exists(controlServerMainPackage.resolve("middleware/cloud")));
    }

    @Test
    void resourceGameRootStaysAggregatorOnly() {
        assertTrue(!Files.exists(RESOURCE_GAME_ROOT.resolve("src/main/java")));
    }

    @Test
    void resourceGameRootDoesNotVendAbstractCacheSources() throws IOException {
        try (var paths = Files.walk(RESOURCE_GAME_ROOT)) {
            long vendoredAbstractCacheSourceCount = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> !path.toString().contains("\\target\\"))
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(this::containsVendoredAbstractCachePackage)
                    .count();

            assertEquals(0, vendoredAbstractCacheSourceCount);
        }
    }

    @Test
    void controlServerDoesNotKeepLegacyLocalCacheOrAsyncTaskDuplicates() {
        assertTrue(!Files.exists(RESOURCE_GAME_ROOT.resolve("control-server/src/main/java/org/tavall/hytale/resourcegame/cache")));
        assertTrue(!Files.exists(RESOURCE_GAME_ROOT.resolve("control-server/src/main/java/com/tavall/resourcegame/tasks/AsyncTask.java")));
        assertTrue(!Files.exists(RESOURCE_GAME_ROOT.resolve("control-server/src/main/java/org/tavall/hytale/resourcegame/concurrent/AsyncTask.java")));
    }

    private boolean containsVendoredAbstractCachePackage(Path path) {
        try {
            String source = Files.readString(path);
            return VENDORED_ABSTRACT_CACHE_PACKAGE.matcher(source).find();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to inspect source file " + path, exception);
        }
    }

    /**
     * Maven runs tests from the active module directory, while IDE runs may start from
     * the aggregator root. Resolve both so module dependency tests do not depend on
     * which runner launched them.
     */
    private static Path resolveResourceGameRoot() {
        Path currentDirectory = Path.of("").toAbsolutePath();
        if (Files.exists(currentDirectory.resolve("game-api"))) {
            return currentDirectory;
        }
        Path parentDirectory = currentDirectory.getParent();
        if (parentDirectory != null && Files.exists(parentDirectory.resolve("game-api"))) {
            return parentDirectory;
        }
        throw new IllegalStateException("Unable to resolve Tavall resource game module root from " + currentDirectory);
    }
}
