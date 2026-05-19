package org.tavall.minecraft.server.resourcepack;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.tavall.minecraft.framework.game.ui.UiScreenKey;
import org.tavall.minecraft.server.IBukkitUtilDependencyAccess;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class MinecraftBukkitResourcePackHandler implements IMinecraftBukkitResourcePackHandler, IBukkitUtilDependencyAccess, IDependencyInjectableConcrete {
    private static final String DEFAULT_PACK_FILE_NAME = "pack.zip";
    private static final String DEFAULT_PACK_PATH = "/resource-pack.zip";
    private static final String DEFAULT_CONTENT_TYPE = "application/zip";
    private static final String DEFAULT_BUNDLED_PACK_DIRECTORY = "distribution";
    private static final String DEFAULT_BUNDLED_PACK_FILE_NAME = "crownbound_minecraft_resource_pack.zip";
    private static final String DEFAULT_BUNDLED_CHECKSUM_FILE_NAME = "crownbound_minecraft_resource_pack.sha256.txt";

    private final Path explicitRoot;
    private final String explicitResourcePackUrl;
    private volatile HttpServer hostedPackServer;
    private volatile byte[] hostedPackArchiveBytes;
    private volatile byte[] hostedPackArchiveHash;

    public MinecraftBukkitResourcePackHandler() {
        this(null, null);
    }

    public static MinecraftBukkitResourcePackHandler forRoot(Path explicitRoot) {
        return new MinecraftBukkitResourcePackHandler(explicitRoot, null);
    }

    public static MinecraftBukkitResourcePackHandler forRoot(Path explicitRoot, URI explicitResourcePackUrl) {
        return new MinecraftBukkitResourcePackHandler(explicitRoot, explicitResourcePackUrl);
    }

    MinecraftBukkitResourcePackHandler(Path explicitRoot, URI explicitResourcePackUrl) {
        this.explicitRoot = explicitRoot;
        this.explicitResourcePackUrl = explicitResourcePackUrl == null ? null : explicitResourcePackUrl.toString();
    }

    @Override
    public Path resourcePackRoot() {
        Path root = explicitRoot != null ? explicitRoot : Path.of(getMinecraftBukkitServerConfig().resourcePackPath());
        return root.toAbsolutePath().normalize();
    }

    @Override
    public Path resourcePackArchive() {
        return resourcePackRoot().resolve(DEFAULT_PACK_FILE_NAME);
    }

    @Override
    public Path castleAssetsRoot() {
        return resourcePackRoot().resolve("castles");
    }

    @Override
    public Path buildingAssetsRoot() {
        return resourcePackRoot().resolve("buildings");
    }

    @Override
    public List<String> castleAssetFiles() {
        return previewAssetFiles(castleAssetsRoot());
    }

    @Override
    public List<String> buildingAssetFiles() {
        return previewAssetFiles(buildingAssetsRoot());
    }

    @Override
    public List<String> expectedCastleAssetFiles(UiScreenKey pageType) {
        if (pageType == null) {
            return List.of();
        }
        return switch (pageType) {
            case CASTLE_MAIN -> List.of("castle_main.png", "castle_main.json");
            case CASTLE_INFO -> List.of("castle_info.png", "castle_info.json");
            case CASTLE_CITIZENS -> List.of("castle_citizens.png", "castle_citizens.json");
            case CASTLE_TROOPS -> List.of("castle_troops.png", "castle_troops.json");
            case CASTLE_RESOURCES -> List.of("castle_resources.png", "castle_resources.json");
            case CASTLE_UPGRADES -> List.of("castle_upgrades.png", "castle_upgrades.json");
            case CASTLE_BUILDINGS -> List.of("castle_buildings.png", "castle_buildings.json");
            default -> List.of();
        };
    }

    @Override
    public List<String> expectedBuildingAssetFiles(UiScreenKey pageType) {
        if (pageType == null) {
            return List.of();
        }
        return switch (pageType) {
            case FARMSTEAD_MENU -> List.of("farmstead.png", "farmstead.json");
            case NPC_MAIN -> List.of("npc_main.png", "npc_main.json");
            case RESOURCE_NODE_DETAIL -> List.of("node_detail.png", "node_detail.json");
            case BUILDING_DETAIL -> List.of("building_detail.png", "building_detail.json");
            case INTERIOR_MAIN -> List.of("interior_main.png", "interior_main.json");
            default -> List.of();
        };
    }

    @Override
    public String resourcePackUrl() {
        URI uri = configuredResourcePackUrl();
        return uri == null ? "" : uri.toString();
    }

    @Override
    public String resourcePackPrompt() {
        return getMinecraftBukkitServerConfig().resourcePackPrompt();
    }

    @Override
    public boolean resourcePackForce() {
        return getMinecraftBukkitServerConfig().resourcePackForce();
    }

    @Override
    public int resourcePackFormat() {
        return getMinecraftBukkitServerConfig().resourcePackFormat();
    }

    @Override
    public byte[] resourcePackHash() {
        URI resourcePackUrl = configuredResourcePackUrl();
        if (resourcePackUrl != null && "https".equalsIgnoreCase(resourcePackUrl.getScheme())) {
            return bundledPackHash();
        }
        ensurePackArchive();
        return hostedPackArchiveHash == null ? new byte[0] : hostedPackArchiveHash.clone();
    }

    @Override
    public void ensureLayout() {
        try {
            Files.createDirectories(resourcePackRoot());
            Files.createDirectories(castleAssetsRoot());
            Files.createDirectories(buildingAssetsRoot());
            ensurePackArchive();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to prepare resource pack layout at " + resourcePackRoot(), exception);
        }
    }

    @Override
    public void startHostedPackServer() {
        URI resourcePackUrl = configuredResourcePackUrl();
        if (resourcePackUrl == null) {
            return;
        }
        if ("https".equalsIgnoreCase(resourcePackUrl.getScheme())) {
            getMinecraftBukkitLogger().info("Skipping local resource pack HTTP server because the configured resource pack URL is externally hosted over HTTPS: " + resourcePackUrl);
            return;
        }
        ensurePackArchive();
        synchronized (this) {
            if (hostedPackServer != null) {
                return;
            }
            int port = resolvePort(resourcePackUrl);
            String contextPath = resolveContextPath(resourcePackUrl);
            try {
                hostedPackServer = HttpServer.create(new InetSocketAddress(port), 0);
                hostedPackServer.createContext(contextPath, this::handlePackRequest);
                hostedPackServer.setExecutor(null);
                hostedPackServer.start();
                getMinecraftBukkitLogger().info("Resource pack HTTP server started at " + resourcePackUrl + " archive=" + resourcePackArchive());
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to start resource pack HTTP server at " + resourcePackUrl, exception);
            }
        }
    }

    @Override
    public void stopHostedPackServer() {
        synchronized (this) {
            if (hostedPackServer != null) {
                hostedPackServer.stop(0);
                hostedPackServer = null;
            }
        }
    }

    @Override
    public void forceResourcePack(Player player) {
        URI resourcePackUrl = configuredResourcePackUrl();
        if (resourcePackUrl == null) {
            return;
        }
        byte[] hash = resourcePackHash();
        getMinecraftBukkitLogger().info("Sending forced resource pack to " + player.getName() + " url=" + resourcePackUrl + " hash=" + bytesToHex(hash));
        player.setResourcePack(
                resourcePackUrl.toString(),
                hash,
                Component.text(resourcePackPrompt()),
                resourcePackForce()
        );
    }

    @Override
    public String statusLine() {
        return "root=" + resourcePackRoot()
                + ", archive=" + resourcePackArchive()
                + ", bundled=" + bundledPackArchive()
                + ", url=" + resourcePackUrl()
                + ", castles=" + castleAssetsRoot()
                + ", buildings=" + buildingAssetsRoot();
    }

    private void handlePackRequest(HttpExchange exchange) throws IOException {
        try (exchange) {
            boolean headRequest = "HEAD".equalsIgnoreCase(exchange.getRequestMethod());
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod()) && !headRequest) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            byte[] bytes = ensurePackArchive();
            exchange.getResponseHeaders().set("Content-Type", DEFAULT_CONTENT_TYPE);
            exchange.getResponseHeaders().set("Content-Length", String.valueOf(bytes.length));
            exchange.sendResponseHeaders(200, headRequest ? -1 : bytes.length);
            if (!headRequest) {
                try (OutputStream outputStream = exchange.getResponseBody()) {
                    outputStream.write(bytes);
                }
            }
        }
    }

    private synchronized byte[] ensurePackArchive() {
        if (hostedPackArchiveBytes != null && hostedPackArchiveHash != null) {
            return hostedPackArchiveBytes.clone();
        }
        try {
            Files.createDirectories(resourcePackRoot());
            Path packArchive = resourcePackArchive();
            hostedPackArchiveBytes = createPackArchiveBytes();
            Files.write(packArchive, hostedPackArchiveBytes);
            hostedPackArchiveHash = sha1(hostedPackArchiveBytes);
            return hostedPackArchiveBytes.clone();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to prepare resource pack archive at " + resourcePackArchive(), exception);
        }
    }

    private byte[] createPackArchiveBytes() throws IOException {
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream, StandardCharsets.UTF_8)) {
            List<Path> localFiles = localPackFiles();
            Set<String> localEntryNames = new HashSet<String>();
            for (Path file : localFiles) {
                localEntryNames.add(resourcePackRoot().relativize(file).toString().replace('\\', '/'));
            }

            boolean wrotePackMetadata = false;
            Path bundledArchive = bundledPackArchive();
            if (Files.isRegularFile(bundledArchive)) {
                byte[] bundledBytes = Files.readAllBytes(bundledArchive);
                validateBundledPackChecksum(bundledBytes);
                wrotePackMetadata = zipBundledArchiveEntries(zipOutputStream, bundledBytes, localEntryNames);
            }

            if (!wrotePackMetadata) {
                ZipEntry metadataEntry = new ZipEntry("pack.mcmeta");
                zipOutputStream.putNextEntry(metadataEntry);
                zipOutputStream.write(packMetadataJson().getBytes(StandardCharsets.UTF_8));
                zipOutputStream.closeEntry();
            }
            zipResourcePackFiles(zipOutputStream, localFiles);
        }
        return outputStream.toByteArray();
    }

    private void zipResourcePackFiles(ZipOutputStream zipOutputStream, List<Path> files) throws IOException {
        for (Path file : files) {
            ZipEntry entry = new ZipEntry(resourcePackRoot().relativize(file).toString().replace('\\', '/'));
            zipOutputStream.putNextEntry(entry);
            Files.copy(file, zipOutputStream);
            zipOutputStream.closeEntry();
        }
    }

    private boolean zipBundledArchiveEntries(ZipOutputStream zipOutputStream, byte[] bundledBytes, Set<String> localOverrides) throws IOException {
        boolean wrotePackMetadata = false;
        try (ZipInputStream zipInputStream = new ZipInputStream(new java.io.ByteArrayInputStream(bundledBytes), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.isDirectory() || localOverrides.contains(entry.getName())) {
                    continue;
                }
                ZipEntry outgoingEntry = new ZipEntry(entry.getName());
                zipOutputStream.putNextEntry(outgoingEntry);
                zipInputStream.transferTo(zipOutputStream);
                zipOutputStream.closeEntry();
                if ("pack.mcmeta".equals(entry.getName())) {
                    wrotePackMetadata = true;
                }
            }
        }
        return wrotePackMetadata;
    }

    private List<Path> localPackFiles() throws IOException {
        if (!Files.exists(resourcePackRoot())) {
            return List.of();
        }
        try (Stream<Path> stream = Files.walk(resourcePackRoot())) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> !Objects.equals(path.getFileName().toString(), DEFAULT_PACK_FILE_NAME))
                    .filter(path -> !Objects.equals(path.getFileName().toString(), "README.md"))
                    .filter(path -> !Objects.equals(path.getFileName().toString(), ".gitkeep"))
                    .filter(path -> !path.startsWith(resourcePackRoot().resolve(DEFAULT_BUNDLED_PACK_DIRECTORY)))
                    .sorted((left, right) -> resourcePackRoot().relativize(left).toString().compareTo(resourcePackRoot().relativize(right).toString()))
                    .toList();
        }
    }

    private String packMetadataJson() {
        String description = escapeJson(getMinecraftBukkitServerConfig().resourcePackPrompt());
        return "{\n"
                + "  \"pack\": {\n"
                + "    \"pack_format\": " + resourcePackFormat() + ",\n"
                + "    \"description\": \"" + description + "\"\n"
                + "  }\n"
                + "}\n";
    }

    private URI configuredResourcePackUrl() {
        if (explicitResourcePackUrl != null && !explicitResourcePackUrl.trim().isEmpty()) {
            return URI.create(explicitResourcePackUrl.trim());
        }
        String configuredUrl = getMinecraftBukkitServerConfig().resourcePackUrl();
        if (configuredUrl == null || configuredUrl.trim().isEmpty()) {
            return null;
        }
        return URI.create(configuredUrl.trim());
    }

    private static int resolvePort(URI resourcePackUrl) {
        int port = resourcePackUrl.getPort();
        if (port > 0) {
            return port;
        }
        return "https".equalsIgnoreCase(resourcePackUrl.getScheme()) ? 443 : 80;
    }

    private static String resolveContextPath(URI resourcePackUrl) {
        String path = resourcePackUrl.getPath();
        if (path == null || path.trim().isEmpty() || "/".equals(path.trim())) {
            return DEFAULT_PACK_PATH;
        }
        if (!path.startsWith("/")) {
            return "/" + path;
        }
        return path;
    }

    private static byte[] sha1(byte[] bytes) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            return messageDigest.digest(bytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-1 digest unavailable.", exception);
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
            throw new IllegalStateException("SHA-256 digest unavailable.", exception);
        }
    }

    private void validateBundledPackChecksum(byte[] bundledBytes) throws IOException {
        Path checksumPath = bundledPackChecksumFile();
        if (!Files.isRegularFile(checksumPath)) {
            return;
        }
        String checksumLine = Files.readString(checksumPath, StandardCharsets.UTF_8).trim();
        if (checksumLine.isBlank()) {
            return;
        }
        String expectedChecksum = checksumLine.split("\\s+")[0].trim().toLowerCase();
        String actualChecksum = sha256Hex(bundledBytes);
        if (!actualChecksum.equals(expectedChecksum)) {
            throw new IllegalStateException("Bundled resource pack checksum mismatch. expected=" + expectedChecksum + " actual=" + actualChecksum + " file=" + bundledPackArchive());
        }
    }

    private byte[] bundledPackHash() {
        Path bundledArchive = bundledPackArchive();
        if (!Files.isRegularFile(bundledArchive)) {
            return new byte[0];
        }
        try {
            byte[] bundledBytes = Files.readAllBytes(bundledArchive);
            validateBundledPackChecksum(bundledBytes);
            return sha1(bundledBytes);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read bundled resource pack archive at " + bundledArchive, exception);
        }
    }

    private Path bundledPackArchive() {
        return resourcePackRoot().resolve(DEFAULT_BUNDLED_PACK_DIRECTORY).resolve(DEFAULT_BUNDLED_PACK_FILE_NAME);
    }

    private Path bundledPackChecksumFile() {
        return resourcePackRoot().resolve(DEFAULT_BUNDLED_PACK_DIRECTORY).resolve(DEFAULT_BUNDLED_CHECKSUM_FILE_NAME);
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(String.format("%02x", value));
        }
        return builder.toString();
    }

    private List<String> previewAssetFiles(Path root) {
        if (root == null || !Files.exists(root)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(root)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .limit(5L)
                    .toList();
        } catch (IOException exception) {
            return List.of();
        }
    }
}
