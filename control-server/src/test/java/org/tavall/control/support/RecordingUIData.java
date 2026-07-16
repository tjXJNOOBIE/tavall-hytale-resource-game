package org.tavall.control.support;

import com.hypixel.hytale.server.core.entity.entities.Player;
import org.tavall.control.api.UIData;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.UiNavigationContext;
import org.tavall.minecraft.framework.game.ui.UiScreenKey;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Test double for tracking UI refresh requests without requiring live player instances.
 */
public final class RecordingUIData extends UIData {
    private final Map<UUID, PlayerGameState> refreshedStates = new ConcurrentHashMap<>();

    @Override
    public void open(UiScreenKey type, Player player, UiNavigationContext context, PlayerGameState state) {
        super.open(type, player, context, state);
        if (player != null && state != null) {
            refreshedStates.put(player.getUuid(), state);
        }
    }

    @Override
    public void refreshTrackedPage(UUID playerId, PlayerGameState state) {
        super.refreshTrackedPage(playerId, state);
        if (playerId != null && state != null) {
            refreshedStates.put(playerId, state);
        }
    }

    @Override
    public void clearTrackedPage(UUID playerId) {
        super.clearTrackedPage(playerId);
        if (playerId != null) {
            refreshedStates.remove(playerId);
        }
    }

    public PlayerGameState lastState(UUID playerId) {
        return refreshedStates.get(playerId);
    }
}
