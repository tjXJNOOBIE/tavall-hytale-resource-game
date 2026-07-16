package org.tavall.minecraft.server.ui.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class GuiManager {
    private final Map<UUID, GuiScreen> openScreens = new ConcurrentHashMap<UUID, GuiScreen>();

    public Inventory openGui(Player player, GuiScreen screen) {
        Inventory inventory = screen.createInventory(player);
        openScreens.put(player.getUniqueId(), screen);
        player.openInventory(inventory);
        return inventory;
    }

    public GuiScreen getOpenScreen(Player player) {
        return openScreens.get(player.getUniqueId());
    }

    public void closeGui(Player player) {
        openScreens.remove(player.getUniqueId());
    }
}
