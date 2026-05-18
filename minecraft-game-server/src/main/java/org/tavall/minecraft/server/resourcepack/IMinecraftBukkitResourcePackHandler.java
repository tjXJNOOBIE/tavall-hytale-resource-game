package org.tavall.minecraft.server.resourcepack;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.tavall.minecraft.framework.game.ui.UiScreenKey;

import java.nio.file.Path;
import java.util.List;

public interface IMinecraftBukkitResourcePackHandler extends IDependencyInjectableInterface {
    Path resourcePackRoot();

    Path castleAssetsRoot();

    Path buildingAssetsRoot();

    List<String> castleAssetFiles();

    List<String> buildingAssetFiles();

    List<String> expectedCastleAssetFiles(UiScreenKey pageType);

    List<String> expectedBuildingAssetFiles(UiScreenKey pageType);

    void ensureLayout();

    String statusLine();
}
