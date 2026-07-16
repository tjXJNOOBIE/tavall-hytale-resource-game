package org.tavall.minecraft.server.ui.inventory;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class GuiScreen {
    private final Component title;
    private final int size;
    private final Map<Integer, GuiButton> buttons = new LinkedHashMap<Integer, GuiButton>();

    protected GuiScreen(Component title, int size) {
        this.title = title;
        this.size = size;
    }

    public Inventory createInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(null, size, title);
        buttons.clear();
        build(player);
        decorate(inventory);
        for (GuiButton button : buttons.values()) {
            inventory.setItem(button.slot(), button.displayItem());
        }
        return inventory;
    }

    protected abstract void build(Player player);

    protected void decorate(Inventory inventory) {
    }

    protected void setButton(GuiButton button) {
        buttons.put(button.slot(), button);
    }

    public GuiButton getButton(int slot) {
        return buttons.get(slot);
    }

    public Component title() {
        return title;
    }

    public int size() {
        return size;
    }
}
