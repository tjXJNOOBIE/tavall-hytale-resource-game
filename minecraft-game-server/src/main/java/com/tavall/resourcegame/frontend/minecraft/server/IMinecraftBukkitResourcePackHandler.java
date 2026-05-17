package com.tavall.resourcegame.frontend.minecraft.server;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import com.tavall.resourcegame.ui.UiPageType;

import java.nio.file.Path;
import java.util.List;

public interface IMinecraftBukkitResourcePackHandler extends IDependencyInjectableInterface {
    Path resourcePackRoot();

    Path castleAssetsRoot();

    Path buildingAssetsRoot();

    List<String> castleAssetFiles();

    List<String> buildingAssetFiles();

    List<String> expectedCastleAssetFiles(UiPageType pageType);

    List<String> expectedBuildingAssetFiles(UiPageType pageType);

    void ensureLayout();

    String statusLine();
}
