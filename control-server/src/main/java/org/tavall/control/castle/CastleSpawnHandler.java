package org.tavall.control.castle;
import org.tavall.control.player.PlayerSessionStore;
import org.tavall.control.player.PlayerSession;

import com.hypixel.hytale.server.core.entity.entities.Player;
import org.tavall.control.config.CastleAssetConfig;
import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.control.castle.ICastleSiteVisualHandler;
import org.tavall.control.castle.ICastleSpawnHandler;
import org.tavall.control.player.IPlayerSessionStore;
import org.tavall.control.domain.CastleLocationData;
import org.tavall.control.domain.PlayerGameState;

import java.util.Objects;
import java.util.UUID;

/**
 * Delegates castle surface refreshes to the block-first castle-site visual service.
 */
public final class CastleSpawnHandler implements ICastleSpawnHandler, IDependencyInjectableConcrete {
    private final CastleAssetConfig assetConfig;
    private final IPlayerSessionStore sessionStore;
    private final ICastleSiteVisualHandler castleSiteVisualHandler;

    public CastleSpawnHandler(
            CastleAssetConfig assetConfig,
            IPlayerSessionStore sessionStore,
            ICastleSiteVisualHandler castleSiteVisualHandler
    ) {
        this.assetConfig = Objects.requireNonNull(assetConfig, "assetConfig");
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.castleSiteVisualHandler = Objects.requireNonNull(castleSiteVisualHandler, "castleSiteVisualHandler");
    }

    @Override
    public void ensureCastleSpawned(Player player, CastleLocationData locationData) {
        if (player == null || locationData == null) {
            return;
        }
        PlayerSession session = sessionStore.get(player.getUuid());
        if (session == null) {
            return;
        }
        castleSiteVisualHandler.ensureSite(player.getUuid(), ensureAssetType(session));
    }

    @Override
    public void replaceCastle(UUID playerId, CastleLocationData locationData) {
        if (playerId == null || locationData == null) {
            return;
        }
        PlayerSession session = sessionStore.get(playerId);
        if (session == null) {
            return;
        }
        castleSiteVisualHandler.refreshSite(playerId, ensureAssetType(session));
    }

    private PlayerGameState ensureAssetType(PlayerSession session) {
        if (session.gameState().castleAssetType() != null && !session.gameState().castleAssetType().isBlank()) {
            return session.gameState();
        }
        PlayerGameState updatedState = session.gameState().withCastleAssetType(assetConfig.structureAssetType(), java.time.Instant.now());
        session.updateGameState(updatedState);
        return updatedState;
    }
}

