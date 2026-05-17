package com.tavall.resourcegame.frontend.minecraft.server;

import com.tavall.resourcegame.api.internal.interaction.InteractionMenuModel;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Objects;

final class MinecraftBukkitInteractionMenuHolder implements InventoryHolder {
    private final MinecraftBukkitInteractionTarget target;
    private final InteractionMenuModel menu;
    private final String feedback;
    private Inventory inventory;

    MinecraftBukkitInteractionMenuHolder(MinecraftBukkitInteractionTarget target, InteractionMenuModel menu, String feedback) {
        this.target = Objects.requireNonNull(target, "target");
        this.menu = Objects.requireNonNull(menu, "menu");
        this.feedback = feedback == null ? "" : feedback;
    }

    MinecraftBukkitInteractionTarget target() {
        return target;
    }

    InteractionMenuModel menu() {
        return menu;
    }

    String feedback() {
        return feedback;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    void inventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
