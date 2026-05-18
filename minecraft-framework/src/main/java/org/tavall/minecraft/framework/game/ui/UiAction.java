package org.tavall.minecraft.framework.game.ui;

public record UiAction(String actionId, String label, boolean enabled, String blockedReason) {
}
