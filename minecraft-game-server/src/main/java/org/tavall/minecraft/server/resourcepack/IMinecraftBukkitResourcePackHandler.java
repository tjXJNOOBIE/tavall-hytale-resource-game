package org.tavall.minecraft.server.resourcepack;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.tavall.minecraft.framework.game.ui.UiScreenKey;
import org.bukkit.entity.Player;

import java.nio.file.Path;
import java.util.List;

public interface IMinecraftBukkitResourcePackHandler extends IDependencyInjectableInterface {
    Path resourcePackRoot();

    Path resourcePackArchive();

    Path castleAssetsRoot();

    Path buildingAssetsRoot();

    List<String> castleAssetFiles();

    List<String> buildingAssetFiles();

    List<String> expectedCastleAssetFiles(UiScreenKey pageType);

    List<String> expectedBuildingAssetFiles(UiScreenKey pageType);

    void ensureLayout();

    void startHostedPackServer();

    void stopHostedPackServer();

    void forceResourcePack(Player player);

    byte[] resourcePackHash();

    String resourcePackUrl();

    String resourcePackPrompt();

    boolean resourcePackForce();

    int resourcePackFormat();

    String statusLine();
}
