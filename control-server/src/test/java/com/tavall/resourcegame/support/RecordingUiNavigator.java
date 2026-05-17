package com.tavall.resourcegame.support;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.tavall.resourcegame.dependency.interfaces.IUiNavigator;
import com.tavall.resourcegame.domain.PlayerGameState;
import com.tavall.resourcegame.domain.UiNavigationContext;
import com.tavall.resourcegame.ui.UiPageType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Test double for tracking UI refresh requests without requiring live player instances.
 */
public final class RecordingUiNavigator implements IUiNavigator {
    private final Map<UUID, PlayerGameState> refreshedStates = new ConcurrentHashMap<>();

    @Override
    public void open(UiPageType type, Player player, UiNavigationContext context, PlayerGameState state) {
        if (player != null && state != null) {
            refreshedStates.put(player.getUuid(), state);
        }
    }

    @Override
    public void refreshTrackedPage(UUID playerId, PlayerGameState state) {
        if (playerId != null && state != null) {
            refreshedStates.put(playerId, state);
        }
    }

    @Override
    public void clearTrackedPage(UUID playerId) {
        if (playerId != null) {
            refreshedStates.remove(playerId);
        }
    }

    public PlayerGameState lastState(UUID playerId) {
        return refreshedStates.get(playerId);
    }
}
