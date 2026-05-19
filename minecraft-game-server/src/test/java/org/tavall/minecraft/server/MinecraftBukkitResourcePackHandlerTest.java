package org.tavall.minecraft.server;

import org.tavall.minecraft.server.resourcepack.MinecraftBukkitResourcePackHandler;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MinecraftBukkitResourcePackHandlerTest {
    @Test
    void ensureLayoutCreatesCastleAndBuildingFolders() throws IOException {
        Path root = Files.createTempDirectory("tavall-resource-pack");
        Path configuredRoot = root.resolve("resource-pack");
        Files.createDirectories(configuredRoot.resolve("castles"));
        Files.createDirectories(configuredRoot.resolve("buildings"));
        Files.writeString(configuredRoot.resolve("castles/castle_main.png"), "castle-main");
        Files.writeString(configuredRoot.resolve("buildings/farmstead.json"), "{\"asset\":\"farmstead\"}");
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

        Set<String> entries = new HashSet<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(configuredRoot.resolve("pack.zip")))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                entries.add(entry.getName());
            }
        }

        assertTrue(entries.contains("pack.mcmeta"));
        assertTrue(entries.contains("castles/castle_main.png"));
        assertTrue(entries.contains("buildings/farmstead.json"));
        assertEquals(3, entries.size());
    }

    @Test
    void ensureLayoutUsesBundledPackWhenPresent() throws IOException {
        Path root = Files.createTempDirectory("tavall-resource-pack-bundled");
        Path configuredRoot = root.resolve("resource-pack");
        Path distributionRoot = configuredRoot.resolve("distribution");
        Path localAssetRoot = configuredRoot.resolve("assets/crownbound/items/ui");
        Files.createDirectories(distributionRoot);
        Files.createDirectories(localAssetRoot);
        Path bundledArchive = distributionRoot.resolve("crownbound_minecraft_resource_pack.zip");
        createBundledArchive(bundledArchive);
        String checksum = sha256Hex(Files.readAllBytes(bundledArchive));
        Files.writeString(distributionRoot.resolve("crownbound_minecraft_resource_pack.sha256.txt"), checksum + "  crownbound_minecraft_resource_pack.zip");
        Files.writeString(localAssetRoot.resolve("button_primary.json"), "{\"model\":{\"type\":\"minecraft:model\",\"model\":\"crownbound:item/ui/button_primary\"}}");

        MinecraftBukkitResourcePackHandler handler = MinecraftBukkitResourcePackHandler.forRoot(
                configuredRoot,
                URI.create("http://127.0.0.1:18182/resource-pack.zip")
        );

        handler.ensureLayout();

        Set<String> entries = new HashSet<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(configuredRoot.resolve("pack.zip")))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                entries.add(entry.getName());
            }
        }

        assertTrue(entries.contains("pack.mcmeta"));
        assertTrue(entries.contains("assets/crownbound/ui/buttons/button_primary.json"));
        assertTrue(entries.contains("assets/crownbound/textures/gui/buttons/button_primary.png"));
        assertTrue(entries.contains("assets/crownbound/items/ui/button_primary.json"));
    }

    private static void createBundledArchive(Path archivePath) throws IOException {
        try (OutputStream fileOutput = Files.newOutputStream(archivePath);
             ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutput)) {
            zipOutputStream.putNextEntry(new ZipEntry("pack.mcmeta"));
            zipOutputStream.write("{\"pack\":{\"pack_format\":34,\"description\":\"Crownbound\"}}".getBytes());
            zipOutputStream.closeEntry();
            zipOutputStream.putNextEntry(new ZipEntry("assets/crownbound/ui/buttons/button_primary.json"));
            zipOutputStream.write("{\"id\":\"button_primary\"}".getBytes());
            zipOutputStream.closeEntry();
            zipOutputStream.putNextEntry(new ZipEntry("assets/crownbound/textures/gui/buttons/button_primary.png"));
            zipOutputStream.write("png".getBytes());
            zipOutputStream.closeEntry();
        }
    }

    private static String sha256Hex(byte[] bytes) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(bytes);
            StringBuilder builder = new StringBuilder(digest.length * 2);
            for (byte digestByte : digest) {
                builder.append(String.format("%02x", digestByte));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
