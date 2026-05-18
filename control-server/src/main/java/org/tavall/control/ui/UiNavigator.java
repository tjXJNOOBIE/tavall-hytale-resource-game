package org.tavall.control.ui;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.api.minecraft.ui.UiPageType;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.TrackedUiState;
import org.tavall.control.domain.UiNavigationContext;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Tracks the latest UI page and page context for a player without owning page rendering.
 */
public final class UiNavigator implements IUiNavigator, IDependencyInjectableConcrete {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final Set<UiPageType> REFRESHABLE_PAGE_TYPES = Set.of(
            UiPageType.CASTLE_MAIN,
            UiPageType.CASTLE_CITIZENS,
            UiPageType.CASTLE_RESOURCES,
            UiPageType.CASTLE_UPGRADES,
            UiPageType.CASTLE_BUILDINGS,
            UiPageType.FARMSTEAD_MENU,
            UiPageType.NPC_MAIN,
            UiPageType.RESOURCE_NODE_DETAIL,
            UiPageType.BUILDING_DETAIL
    );

    private final ConcurrentHashMap<UUID, TrackedUiState> trackedPages = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Long> recentOpenTimes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, UiPageType> recentOpenTypes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, String> recentOpenFingerprints = new ConcurrentHashMap<>();

    public UiNavigator() {
    }

    public void open(UiPageType type, Player player, UiNavigationContext context, PlayerGameState state) {
        if (type == null || player == null) {
            return;
        }
        UUID playerId = player.getUuid();
        TrackedUiState trackedState = new TrackedUiState(type, context, stateFingerprint(state));
        trackedPages.put(playerId, trackedState);
        rememberRecentOpen(playerId, trackedState);
        LOGGER.at(Level.INFO).log("Tracked UI page %s for %s.", type, player.getDisplayName());
    }

    @Override
    public void refreshTrackedPage(UUID playerId, PlayerGameState state) {
        TrackedUiState trackedUiState = trackedPages.get(playerId);
        if (trackedUiState == null || !REFRESHABLE_PAGE_TYPES.contains(trackedUiState.pageType())) {
            return;
        }
        TrackedUiState refreshedState = new TrackedUiState(
                trackedUiState.pageType(),
                trackedUiState.navigationContext(),
                stateFingerprint(state)
        );
        trackedPages.put(playerId, refreshedState);
        rememberRecentOpen(playerId, refreshedState);
        LOGGER.at(Level.INFO).log("Refreshed tracked UI page %s for %s.", trackedUiState.pageType(), playerId);
    }

    @Override
    public void clearTrackedPage(UUID playerId) {
        if (playerId != null) {
            trackedPages.remove(playerId);
            recentOpenTimes.remove(playerId);
            recentOpenTypes.remove(playerId);
            recentOpenFingerprints.remove(playerId);
        }
    }

    private void rememberRecentOpen(UUID playerId, TrackedUiState trackedState) {
        if (playerId == null || trackedState == null) {
            return;
        }
        recentOpenTimes.put(playerId, System.currentTimeMillis());
        recentOpenTypes.put(playerId, trackedState.pageType());
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

