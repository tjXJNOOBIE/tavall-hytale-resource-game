package org.tavall.control.support;

import org.tavall.control.dependency.interfaces.ICastleSiteVisualService;
import org.tavall.control.domain.PlayerGameState;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RecordingCastleSiteVisualService implements ICastleSiteVisualService {
    private final Map<UUID, PlayerGameState> lastState = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> refreshCount = new ConcurrentHashMap<>();

    @Override
    public void ensureSite(UUID playerId, PlayerGameState state) {
        if (playerId == null || state == null) {
            return;
        }
        lastState.put(playerId, state);
        refreshCount.merge(playerId, 1, Integer::sum);
    }

    @Override
    public void refreshSite(UUID playerId, PlayerGameState state) {
        ensureSite(playerId, state);
    }

    @Override
    public void clearSite(UUID playerId) {
        lastState.remove(playerId);
        refreshCount.remove(playerId);
    }

    public PlayerGameState lastState(UUID playerId) {
        return lastState.get(playerId);
    }

    public int refreshCount(UUID playerId) {
        return refreshCount.getOrDefault(playerId, 0);
    }
}
