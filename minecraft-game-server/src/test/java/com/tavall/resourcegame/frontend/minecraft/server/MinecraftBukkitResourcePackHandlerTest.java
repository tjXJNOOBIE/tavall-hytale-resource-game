package org.tavall.minecraft.server;

import org.tavall.minecraft.server.resourcepack.MinecraftBukkitResourcePackHandler;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class MinecraftBukkitResourcePackHandlerTest {
    @Test
    void ensureLayoutCreatesCastleAndBuildingFolders() throws IOException {
        Path root = Files.createTempDirectory("tavall-resource-pack");
        Path configuredRoot = root.resolve("resource-pack");
        MinecraftBukkitResourcePackHandler handler = MinecraftBukkitResourcePackHandler.forRoot(configuredRoot);

        handler.ensureLayout();

        assertTrue(Files.isDirectory(configuredRoot));
        assertTrue(Files.isDirectory(configuredRoot.resolve("castles")));
        assertTrue(Files.isDirectory(configuredRoot.resolve("buildings")));
        assertTrue(handler.statusLine().contains("castles="));
        assertTrue(handler.statusLine().contains("buildings="));
    }
}
