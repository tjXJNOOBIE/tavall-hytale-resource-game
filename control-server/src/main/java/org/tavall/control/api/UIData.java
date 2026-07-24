package org.tavall.control.api;

import com.hypixel.hytale.server.core.entity.entities.Player;
import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.minecraft.framework.game.ui.UiScreenKey;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.TrackedUiState;
import org.tavall.control.domain.UiNavigationContext;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tracks the latest UI page and page context for a player without owning page rendering.
 */
public class UIData implements IDependencyInjectableConcrete {
    private static final Logger LOGGER = Logger.getLogger(UIData.class.getName());
    private static final Set<UiScreenKey> REFRESHABLE_SCREEN_KEYS = Set.of(
            UiScreenKey.CASTLE_MAIN,
            UiScreenKey.CASTLE_CITIZENS,
            UiScreenKey.CASTLE_RESOURCES,
            UiScreenKey.CASTLE_UPGRADES,
            UiScreenKey.CASTLE_BUILDINGS,
            UiScreenKey.FARMSTEAD_MENU,
            UiScreenKey.NPC_MAIN,
            UiScreenKey.RESOURCE_NODE_DETAIL,
            UiScreenKey.BUILDING_DETAIL
    );

    private final ConcurrentHashMap<UUID, TrackedUiState> trackedPages = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Long> recentOpenTimes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, UiScreenKey> recentOpenScreenKeys = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, String> recentOpenFingerprints = new ConcurrentHashMap<>();

    public UIData() {
    }

    public void open(UiScreenKey screenKey, Player player, UiNavigationContext context, PlayerGameState state) {
        if (screenKey == null || player == null) {
            return;
        }
        UUID playerId = player.getUuid();
        TrackedUiState trackedState = new TrackedUiState(screenKey, context, stateFingerprint(state));
        trackedPages.put(playerId, trackedState);
        rememberRecentOpen(playerId, trackedState);
        LOGGER.log(Level.INFO, String.format("Tracked UI screen %s for %s.", screenKey, player.getDisplayName()));
    }

    public void refreshTrackedPage(UUID playerId, PlayerGameState state) {
        TrackedUiState trackedUiState = trackedPages.get(playerId);
        if (trackedUiState == null || !REFRESHABLE_SCREEN_KEYS.contains(trackedUiState.screenKey())) {
            return;
        }
        TrackedUiState refreshedState = new TrackedUiState(
                trackedUiState.screenKey(),
                trackedUiState.navigationContext(),
                stateFingerprint(state)
        );
        trackedPages.put(playerId, refreshedState);
        rememberRecentOpen(playerId, refreshedState);
        LOGGER.log(Level.INFO, String.format("Refreshed tracked UI screen %s for %s.", trackedUiState.screenKey(), playerId));
    }

    public void clearTrackedPage(UUID playerId) {
        if (playerId != null) {
            trackedPages.remove(playerId);
            recentOpenTimes.remove(playerId);
            recentOpenScreenKeys.remove(playerId);
            recentOpenFingerprints.remove(playerId);
        }
    }

    private void rememberRecentOpen(UUID playerId, TrackedUiState trackedState) {
        if (playerId == null || trackedState == null) {
            return;
        }
        recentOpenTimes.put(playerId, System.currentTimeMillis());
        recentOpenScreenKeys.put(playerId, trackedState.screenKey());
        recentOpenFingerprints.put(playerId, contextFingerprint(trackedState));
    }

    private String contextFingerprint(TrackedUiState trackedState) {
        if (trackedState == null || trackedState.navigationContext() == null) {
            return "";
        }
        UiNavigationContext context = trackedState.navigationContext();
        return String.join(
                "|",
                String.valueOf(context.playerId()),
                String.valueOf(context.playerName()),
                String.valueOf(context.feedbackMessage()),
                String.valueOf(context.selectedNodeId()),
                String.valueOf(context.selectedBuildingId()),
                trackedState.stateFingerprint()
        );
    }

    private String stateFingerprint(PlayerGameState state) {
        if (state == null) {
            return "";
        }
        int metadataHash = state.metadataJson() == null ? 0 : state.metadataJson().hashCode();
        return String.join(
                "|",
                String.valueOf(state.updatedAt()),
                String.valueOf(state.resources().food()),
                String.valueOf(state.resources().wood()),
                String.valueOf(state.resources().iron()),
                String.valueOf(state.populationSummary().citizenCount()),
                String.valueOf(state.populationSummary().troopCount()),
                String.valueOf(metadataHash)
        );
    }
}
