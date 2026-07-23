package org.tavall.control.modules;

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
    void minecraftMainFrontendModulesUseSharedContractsAndTavallToolsThroughGradleDependencies() throws IOException {
        String build = Files.readString(RESOURCE_GAME_ROOT.resolve("build.gradle.kts"));

        assertTrue(build.contains("project(\":game-api\")"));
        assertTrue(build.contains("org.tavall:tavall-logging"));
        assertTrue(build.contains("org.tavall:tavall-di"));
    }

    @Test
    void controlServerUsesTavallToolingAndCacheThroughGradleDependencies() throws IOException {
        String build = Files.readString(RESOURCE_GAME_ROOT.resolve("build.gradle.kts"));

        assertTrue(build.contains("org.tavall:tavall-logging"));
        assertTrue(build.contains("org.tavall:tavall-di"));
        assertTrue(build.contains("org.tavall:tavall-eventbus"));
        assertTrue(build.contains("org.tavall:tavall-concurrency"));
        assertTrue(build.contains("org.tavall:tavall-scheduler"));
        assertTrue(build.contains("org.tavall:abstract-cache-semantic"));
        assertTrue(build.contains("org.tavall:abstract-cache-storage-memory"));
    }

    @Test
    void minecraftMainPrunesLegacyPlatformAndSplitBackendModulesFromAggregator() throws IOException {
        String settings = Files.readString(RESOURCE_GAME_ROOT.resolve("settings.gradle.kts"));

        assertTrue(settings.contains("\"control-server\""));
        assertTrue(settings.contains("\"minecraft-proxy\""));
        assertTrue(settings.contains("\"minecraft-game-server\""));
        assertTrue(!settings.contains("\"core\""));
        assertTrue(!settings.contains("\"cloud-core\""));
        assertTrue(!settings.contains("\"cloud-agent\""));
        assertTrue(!settings.contains("\"cloud-control-plane\""));
        assertTrue(!settings.contains("\"events\""));
        assertTrue(!settings.contains("\"liveops\""));
        assertTrue(!settings.contains("\"hytale-frontend\""));
        assertTrue(!settings.contains("\"roblox-frontend\""));
        assertTrue(!settings.contains("\"pc-app\""));
        assertTrue(!settings.contains("\"android-app\""));
        assertTrue(!settings.contains("\"discord-frontend\""));
        assertTrue(!Files.exists(RESOURCE_GAME_ROOT.resolve("core")));
        assertTrue(!Files.exists(RESOURCE_GAME_ROOT.resolve("cloud-core")));
        assertTrue(!Files.exists(RESOURCE_GAME_ROOT.resolve("cloud-agent")));
        assertTrue(!Files.exists(RESOURCE_GAME_ROOT.resolve("cloud-control-plane")));
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
        String build = Files.readString(RESOURCE_GAME_ROOT.resolve("build.gradle.kts"));
        Path controlServerMainPackage = RESOURCE_GAME_ROOT
                .resolve("control-server")
                .resolve("src/main/java/org/tavall/control");

        assertTrue(!build.contains("project(\":core\")"));
        assertTrue(!build.contains("project(\":cloud-core\")"));
        assertTrue(!build.contains("project(\":cloud-agent\")"));
        assertTrue(!build.contains("project(\":cloud-control-plane\")"));
        assertTrue(!build.contains("project(\":events\")"));
        assertTrue(!build.contains("project(\":liveops\")"));
        assertTrue(Files.exists(controlServerMainPackage.resolve("distribution")));
        assertTrue(Files.exists(controlServerMainPackage.resolve("events")));
        assertTrue(Files.exists(controlServerMainPackage.resolve("liveops")));
        assertTrue(Files.exists(controlServerMainPackage.resolve("cloud")));
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
                    .filter(path -> !path.toString().contains("\\build\\"))
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(this::containsVendoredAbstractCachePackage)
                    .count();

            assertEquals(0, vendoredAbstractCacheSourceCount);
        }
    }

    @Test
    void controlServerDoesNotKeepLegacyLocalCacheOrAsyncTaskDuplicates() {
        assertTrue(!Files.exists(RESOURCE_GAME_ROOT.resolve("control-server/src/main/java/org/tavall/hytale/resourcegame/cache")));
        assertTrue(!Files.exists(RESOURCE_GAME_ROOT.resolve("control-server/src/main/java/org/tavall/control/tasks/AsyncTask.java")));
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
     * Gradle runs tests from the active module directory, while IDE runs may start from
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
