package org.tavall.minecraft.server.ui.inventory;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.tavall.api.minecraft.frontend.FrontendCommandVerificationResult;
import org.tavall.minecraft.framework.game.ui.UiScreenKey;
import org.tavall.minecraft.server.IBukkitUtilDependencyAccess;
import org.tavall.minecraft.server.KingdomAccountGui;
import org.tavall.minecraft.server.KingdomBuildingGui;
import org.tavall.minecraft.server.KingdomDebugGui;
import org.tavall.minecraft.server.MinecraftBukkitServerPlugin;
import org.tavall.minecraft.server.KingdomNpcGui;
import org.tavall.minecraft.server.KingdomPlacementGui;
import org.tavall.minecraft.server.commands.support.KingdomCommandSupport;

import java.util.UUID;

public final class CommandCenterGuiActionHandler implements IBukkitUtilDependencyAccess, IDependencyInjectableConcrete {
    private final KingdomAccountGui accountGui = new KingdomAccountGui();
    private final KingdomBuildingGui buildingGui = new KingdomBuildingGui();
    private final KingdomNpcGui npcGui = new KingdomNpcGui();
    private final KingdomPlacementGui placementGui = new KingdomPlacementGui();
    private final KingdomDebugGui debugGui = new KingdomDebugGui();

    public void openAccount(Player player) {
        runNextTick(() -> accountGui.open(player, "kd ui account"));
    }

    public void openCastle(Player player) {
        runNextTick(() -> getKingdomInventoryUiHandler().open(player, UiScreenKey.CASTLE_MAIN, "Castle overview."));
    }

    public void openCitizens(Player player) {
        runNextTick(() -> getKingdomInventoryUiHandler().open(player, UiScreenKey.CASTLE_CITIZENS, "Citizens."));
    }

    public void openTroops(Player player) {
        runNextTick(() -> getKingdomInventoryUiHandler().open(player, UiScreenKey.CASTLE_TROOPS, "Troops."));
    }

    public void openResources(Player player) {
        runNextTick(() -> getKingdomInventoryUiHandler().open(player, UiScreenKey.CASTLE_RESOURCES, "Resources."));
    }

    public void openBuildings(Player player) {
        runNextTick(() -> getKingdomInventoryUiHandler().open(player, UiScreenKey.CASTLE_BUILDINGS, "Buildings."));
    }

    public void openInterior(Player player) {
        runNextTick(() -> getKingdomInventoryUiHandler().open(player, UiScreenKey.INTERIOR_MAIN, "Interior."));
    }

    public void openFocusedBuilding(Player player) {
        runNextTick(() -> buildingGui.open(player));
    }

    public void openNpc(Player player) {
        runNextTick(() -> npcGui.open(player));
    }

    public void openPlacement(Player player) {
        runNextTick(() -> placementGui.open(player));
    }

    public void openDebug(Player player) {
        runNextTick(() -> debugGui.open(player));
    }

    public void refreshScene(Player player) {
        sendCommand(player, "kd scene refresh", "Scene refresh failed.");
    }

    public void resetTutorial(Player player) {
        sendCommand(player, "kd tutorial reset", "Tutorial reset failed.");
    }

    private void sendCommand(Player player, String rawCommand, String failurePrefix) {
        try {
            FrontendCommandVerificationResult result = getMinecraftBukkitCommandClientHandler().submitCommand(
                    player.getUniqueId().toString(),
                    player.getName(),
                    rawCommand,
                    "minecraft-bukkit-ui-" + UUID.randomUUID(),
                    KingdomCommandSupport.commandMetadata(this, player, "kd")
            );
            KingdomCommandSupport.renderFeedback(this, player, rawCommand, result);
        } catch (Exception exception) {
            player.sendMessage(ChatColor.RED + failurePrefix + " " + exception.getMessage());
        }
    }

    private void runNextTick(Runnable runnable) {
        Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(MinecraftBukkitServerPlugin.class), runnable);
    }
}
