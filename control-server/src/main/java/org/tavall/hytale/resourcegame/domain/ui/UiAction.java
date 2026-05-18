package org.tavall.minecraft.domain.ui;

public record UiAction(String actionId, String label, boolean enabled, String blockedReason) {
}
