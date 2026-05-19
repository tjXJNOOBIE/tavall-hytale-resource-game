package org.tavall.minecraft.server.ui.inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public final class KingdomCommandCenterGuiScreen extends GuiScreen {
    private final CrownboundGuiItemFactory itemFactory;
    private final CommandCenterGuiActionHandler actions;
    private final String feedback;

    public KingdomCommandCenterGuiScreen(CrownboundGuiItemFactory itemFactory, CommandCenterGuiActionHandler actions, String feedback) {
        super(itemFactory.commandCenterTitle(), 45);
        this.itemFactory = itemFactory;
        this.actions = actions;
        this.feedback = feedback == null || feedback.isBlank() ? "Crownbound command center." : feedback;
    }

    @Override
    protected void build(Player player) {
        setButton(new GuiButton(4, itemFactory.header("Kingdom Command Center", feedback), context -> {
        }));
        setButton(new GuiButton(10, itemFactory.button(Material.PLAYER_HEAD, "Account", List.of("Open your player profile."), CrownboundGuiItemFactory.FAMILY_TAB), context -> actions.openAccount(context.player())));
        setButton(new GuiButton(11, itemFactory.button(Material.BEACON, "Castle", List.of("Open the castle overview."), CrownboundGuiItemFactory.FAMILY_TAB), context -> actions.openCastle(context.player())));
        setButton(new GuiButton(12, itemFactory.button(Material.BOOK, "Citizens", List.of("Review staffing and citizen progression."), CrownboundGuiItemFactory.FAMILY_TAB), context -> actions.openCitizens(context.player())));
        setButton(new GuiButton(13, itemFactory.button(Material.IRON_SWORD, "Troops", List.of("Review troop health and promotion tools."), CrownboundGuiItemFactory.FAMILY_TAB), context -> actions.openTroops(context.player())));
        setButton(new GuiButton(14, itemFactory.button(Material.EMERALD, "Resources", List.of("Inspect kingdom resource controls."), CrownboundGuiItemFactory.FAMILY_TAB), context -> actions.openResources(context.player())));
        setButton(new GuiButton(15, itemFactory.button(Material.ANVIL, "Buildings", List.of("Stage and manage kingdom buildings."), CrownboundGuiItemFactory.FAMILY_TAB), context -> actions.openBuildings(context.player())));
        setButton(new GuiButton(16, itemFactory.button(Material.END_PORTAL_FRAME, "Interior", List.of("Enter the interior flow."), CrownboundGuiItemFactory.FAMILY_TAB), context -> actions.openInterior(context.player())));
        setButton(new GuiButton(19, itemFactory.button(Material.CRAFTING_TABLE, "Focused Building", List.of("Open the focused building detail UI."), CrownboundGuiItemFactory.FAMILY_PRIMARY), context -> actions.openFocusedBuilding(context.player())));
        setButton(new GuiButton(20, itemFactory.button(Material.VILLAGER_SPAWN_EGG, "NPC Interaction", List.of("Open NPC and building interaction UI."), CrownboundGuiItemFactory.FAMILY_PRIMARY), context -> actions.openNpc(context.player())));
        setButton(new GuiButton(21, itemFactory.button(Material.SCAFFOLDING, "Placement Tools", List.of("Arm placement and confirmation actions."), CrownboundGuiItemFactory.FAMILY_SUCCESS), context -> actions.openPlacement(context.player())));
        setButton(new GuiButton(22, itemFactory.button(Material.MAP, "Scene Refresh", List.of("Refresh the kingdom scene in-world."), CrownboundGuiItemFactory.FAMILY_SUCCESS), context -> actions.refreshScene(context.player())));
        setButton(new GuiButton(23, itemFactory.button(Material.RECOVERY_COMPASS, "Debug Surfaces", List.of("Open the debug-focused UI screens."), CrownboundGuiItemFactory.FAMILY_ICON), context -> actions.openDebug(context.player())));
        setButton(new GuiButton(24, itemFactory.button(Material.WRITABLE_BOOK, "Tutorial Reset", List.of("Reset the tutorial state for this player."), CrownboundGuiItemFactory.FAMILY_SECONDARY), context -> actions.resetTutorial(context.player())));
        setButton(new GuiButton(40, itemFactory.button(Material.BARRIER, "Close", List.of("Close the command center."), CrownboundGuiItemFactory.FAMILY_DANGER), context -> context.player().closeInventory()));
    }
}
