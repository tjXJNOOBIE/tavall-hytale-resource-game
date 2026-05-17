package com.tavall.resourcegame.frontend.minecraft.server;

import com.tavall.resourcegame.ui.UiPageType;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public final class MinecraftBukkitResourcePackHandler implements IMinecraftBukkitResourcePackHandler, IMinecraftBukkitServerDomain, IDependencyInjectableConcrete {
    private final Path explicitRoot;

    public MinecraftBukkitResourcePackHandler() {
        this(null);
    }

    MinecraftBukkitResourcePackHandler(Path explicitRoot) {
        this.explicitRoot = explicitRoot;
    }

    @Override
    public Path resourcePackRoot() {
        Path root = explicitRoot != null ? explicitRoot : Path.of(getMinecraftBukkitServerConfig().resourcePackPath());
        return root.toAbsolutePath().normalize();
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
    public List<String> expectedCastleAssetFiles(UiPageType pageType) {
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
    public List<String> expectedBuildingAssetFiles(UiPageType pageType) {
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
    public void ensureLayout() {
        try {
            Files.createDirectories(resourcePackRoot());
            Files.createDirectories(castleAssetsRoot());
            Files.createDirectories(buildingAssetsRoot());
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to prepare resource pack layout at " + resourcePackRoot(), exception);
        }
    }

    @Override
    public String statusLine() {
        return "root=" + resourcePackRoot() + ", castles=" + castleAssetsRoot() + ", buildings=" + buildingAssetsRoot();
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
