package org.tavall.minecraft.server;

import org.tavall.minecraft.server.resourcepack.MinecraftBukkitResourcePackHandler;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class MinecraftBukkitResourcePackHandlerTest {
    @Test
    void ensureLayoutCreatesCastleAndBuildingFolders() throws IOException {
        Path root = Files.createTempDirectory("tavall-resource-pack");
        Path configuredRoot = root.resolve("resource-pack");
        MinecraftBukkitResourcePackHandler handler = MinecraftBukkitResourcePackHandler.forRoot(
                configuredRoot,
                URI.create("http://127.0.0.1:18182/resource-pack.zip")
        );

        handler.ensureLayout();

        assertTrue(Files.isDirectory(configuredRoot));
        assertTrue(Files.isDirectory(configuredRoot.resolve("castles")));
        assertTrue(Files.isDirectory(configuredRoot.resolve("buildings")));
        assertTrue(Files.isRegularFile(configuredRoot.resolve("pack.zip")));
        assertTrue(handler.resourcePackHash().length > 0);
        assertTrue(handler.resourcePackArchive().getFileName().toString().equals("pack.zip"));
        assertTrue(handler.statusLine().contains("url=http://127.0.0.1:18182/resource-pack.zip"));
        assertTrue(handler.statusLine().contains("castles="));
        assertTrue(handler.statusLine().contains("buildings="));
    }
}
