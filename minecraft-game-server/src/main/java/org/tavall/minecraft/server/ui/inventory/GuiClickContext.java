package org.tavall.minecraft.server.ui.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public final class GuiClickContext {
    private final Player player;
    private final InventoryClickEvent event;
    private final GuiScreen screen;
    private final int slot;

    public GuiClickContext(Player player, InventoryClickEvent event, GuiScreen screen, int slot) {
        this.player = player;
        this.event = event;
        this.screen = screen;
        this.slot = slot;
    }

    public Player player() {
        return player;
    }

    public InventoryClickEvent event() {
        return event;
    }

    public GuiScreen screen() {
        return screen;
    }

    public int slot() {
        return slot;
    }
}
