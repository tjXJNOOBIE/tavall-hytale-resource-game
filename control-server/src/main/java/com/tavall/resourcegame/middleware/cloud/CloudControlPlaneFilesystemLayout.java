package com.tavall.resourcegame.middleware.cloud;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class CloudControlPlaneFilesystemLayout {
    private static final String DEFAULT_ROOT_DIRECTORY_NAME = "tavall-control-plane";
    private static final String DEFAULT_CONTROL_PLANE_SESSION_NAME = "cloud-control-plane";
    private static final String DEFAULT_KINGDOM_ID = "kingdom-1";

    private final Path root;

    public CloudControlPlaneFilesystemLayout(Path root) {
        this.root = root == null ? defaultRoot() : root;
    }

    public static CloudControlPlaneFilesystemLayout fromEnvironment() {
        String configuredRoot = firstNonBlank(
                System.getenv("TAVALL_CLOUD_CONTROL_PLANE_ROOT"),
                System.getenv("RESOURCE_GAME_CLOUD_CONTROL_PLANE_ROOT"),
                System.getenv("RESOURCE_GAME_CONTROL_PLANE_ROOT"));
        return new CloudControlPlaneFilesystemLayout(configuredRoot == null ? defaultRoot() : Path.of(configuredRoot));
    }

    public Path root() {
        return root;
    }

    public Path logsDir() {
        return root.resolve("logs");
    }

    public Path kingdomsDir() {
        return root.resolve("kingdoms");
    }

    public Path workloadsDir() {
        return root.resolve("workloads");
    }

    public Path consolesDir() {
        return root.resolve("consoles");
    }

    public Path cacheDir() {
        return root.resolve("cache");
    }

    public Path kingdomDir(String kingdomId) {
        return kingdomsDir().resolve(normalizeToken(kingdomId, DEFAULT_KINGDOM_ID));
    }

    public Path ensureKingdom(String kingdomId) {
        Path kingdomDir = kingdomDir(kingdomId);
        createDirectories(kingdomDir);
        return kingdomDir;
    }

    public void ensureLayout() {
        createDirectories(root);
        createDirectories(logsDir());
        createDirectories(kingdomsDir());
        createDirectories(workloadsDir());
        createDirectories(consolesDir());
        createDirectories(cacheDir());
        ensureKingdom(DEFAULT_KINGDOM_ID);
    }

    public List<String> kingdomIds() {
        if (!Files.exists(kingdomsDir())) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(kingdomsDir())) {
            return stream
                    .filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to list kingdom directories under " + kingdomsDir(), exception);
        }
    }

    public String controlPlaneSessionName() {
        String configured = firstNonBlank(
                System.getenv("TAVALL_CLOUD_CONTROL_PLANE_SESSION"),
                System.getenv("RESOURCE_GAME_CLOUD_CONTROL_PLANE_SESSION"),
                System.getenv("RESOURCE_GAME_CONTROL_PLANE_SESSION"));
        return normalizeToken(configured, DEFAULT_CONTROL_PLANE_SESSION_NAME);
    }

    public String workloadSessionName(CloudWorkload workload) {
        if (workload == null) {
            return normalizeToken(null, "workload");
        }
        String configured = Optional.ofNullable(workload.metadata().get("sessionName"))
                .filter(value -> !value.isBlank())
                .orElse(workload.name());
        return normalizeToken(configured, normalizeToken(workload.name(), "workload"));
    }

    public String workloadSessionName(String workloadName) {
        return normalizeToken(workloadName, "workload");
    }

    public String attachCommand(String sessionName) {
        return "tmux attach -t " + normalizeToken(sessionName, DEFAULT_CONTROL_PLANE_SESSION_NAME);
    }

    public static String normalizeToken(String value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        String normalized = value.trim()
                .replaceAll("[^A-Za-z0-9._-]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-+", "")
                .replaceAll("-+$", "");
        return normalized.isBlank() ? defaultValue : normalized;
    }

    private static Path defaultRoot() {
        return Path.of(System.getProperty("java.io.tmpdir"), DEFAULT_ROOT_DIRECTORY_NAME);
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static void createDirectories(Path path) {
        try {
            Files.createDirectories(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create control plane directory " + path, exception);
        }
    }
}
