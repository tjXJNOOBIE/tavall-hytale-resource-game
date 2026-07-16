package org.tavall.minecraft.server.ui.inventory;

@FunctionalInterface
public interface GuiClickAction {
    void onClick(GuiClickContext context);
}
