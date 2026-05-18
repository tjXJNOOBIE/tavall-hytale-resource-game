package org.tavall.control.services;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import org.tavall.control.config.CastleAssetConfig;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.dependency.interfaces.ICastleInteractionService;
import org.tavall.control.dependency.interfaces.IPlayerSessionStore;
import org.tavall.control.dependency.interfaces.IUiNavigator;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.UiNavigationContext;
import org.tavall.control.ui.UiPageType;
import org.tavall.control.world.VectorMath;

import java.util.Objects;
import java.util.UUID;

/**
 * Detects castle interactions and opens the main UI.
 */
public final class CastleInteractionService implements ICastleInteractionService, IDependencyInjectableConcrete {
    private final IPlayerSessionStore sessionStore;
    private final IUiNavigator uiNavigator;
    private final CastleAssetConfig assetConfig;

    public CastleInteractionService(
            IPlayerSessionStore sessionStore,
            IUiNavigator uiNavigator,
            CastleAssetConfig assetConfig
    ) {
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.uiNavigator = Objects.requireNonNull(uiNavigator, "uiNavigator");
        this.assetConfig = Objects.requireNonNull(assetConfig, "assetConfig");
    }

    public void handleInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if (!isPlayerFocusingOwnedCastle(player)) {
            return;
        }
        openCastleUi(player);
    }

    public boolean isPlayerFocusingOwnedCastle(Player player) {
        Vector3d castlePos = resolveCastlePosition(player);
        if (castlePos == null) {
            return false;
        }
        TransformComponent playerTransform = player.getTransformComponent();
        if (playerTransform == null) {
            return false;
        }
        Vector3d playerPos = playerTransform.getPosition();
        double distance = playerPos.distanceTo(castlePos);
        if (distance > assetConfig.interactionDistance()) {
            return false;
        }
        Vector3f rotation = playerTransform.getRotation();
        Vector3d lookVector = VectorMath.lookVector(rotation);
        Vector3d toCastle = new Vector3d(castlePos.getX() - playerPos.getX(), castlePos.getY() - playerPos.getY(), castlePos.getZ() - playerPos.getZ());
        Vector3d toCastleNorm = VectorMath.normalize(toCastle);
        double dot = VectorMath.dot(lookVector, toCastleNorm);
        return dot >= assetConfig.lookDotThreshold();
    }

    public void openCastleUi(Player player) {
        UUID playerId = player.getUuid();
        PlayerSession session = sessionStore.get(playerId);
        if (session == null) {
            return;
        }
        PlayerGameState state = session.gameState();
        uiNavigator.open(UiPageType.CASTLE_MAIN, player, new UiNavigationContext(playerId, player.getDisplayName()), state);
    }

    private Vector3d resolveCastlePosition(Player player) {
        PlayerSession session = sessionStore.get(player.getUuid());
        if (session == null || session.gameState().castleLocation() == null) {
            return null;
        }
        var castleLocation = session.gameState().castleLocation();
        return new Vector3d(castleLocation.x(), castleLocation.y(), castleLocation.z());
    }
}
