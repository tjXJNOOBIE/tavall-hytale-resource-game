package org.tavall.control.npc;
import org.tavall.control.population.PopulationDisplayHandler;
import org.tavall.control.player.PlayerSessionStore;
import org.tavall.control.player.PlayerSession;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.player.IPlayerSessionStore;
import org.tavall.control.api.UIData;
import org.tavall.control.npc.IWorkerNpcInteractionHandler;
import org.tavall.control.domain.CitizenJobType;
import org.tavall.control.domain.UiNavigationContext;
import org.tavall.minecraft.framework.game.ui.UiScreenKey;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Opens worker-focused debug menus from stationary interior worker anchors.
 */
public final class WorkerNpcInteractionHandler implements IWorkerNpcInteractionHandler, IDependencyInjectableConcrete {
    private final PopulationDisplayHandler populationDisplayHandler;
    private final IPlayerSessionStore sessionStore;
    private final UIData uiNavigator;

    public WorkerNpcInteractionHandler(
            PopulationDisplayHandler populationDisplayHandler,
            IPlayerSessionStore sessionStore,
            UIData uiNavigator
    ) {
        this.populationDisplayHandler = Objects.requireNonNull(populationDisplayHandler, "populationDisplayHandler");
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.uiNavigator = Objects.requireNonNull(uiNavigator, "uiNavigator");
    }

    @Override
    public void handleInteract(PlayerInteractEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }
        openFromTarget(event.getPlayer(), event.getTargetRef());
    }

    @Override
    public boolean openFromTarget(Player player, Ref<EntityStore> targetRef) {
        if (player == null) {
            return false;
        }
        UUID playerId = player.getUuid();
        PlayerSession session = sessionStore.get(playerId);
        if (session == null) {
            return false;
        }
        Optional<CitizenJobType> workerType = populationDisplayHandler.resolveWorkerType(playerId, targetRef);
        if (workerType.isPresent()) {
            openWorkerPage(player, session, workerType.get());
            return true;
        }
        if (populationDisplayHandler.isTroopAnchor(playerId, targetRef)) {
            uiNavigator.open(
                    UiScreenKey.CASTLE_TROOPS,
                    player,
                    new UiNavigationContext(playerId, player.getDisplayName()).withFeedback("Troop anchor selected. Might is tier-weighted; current aggregate troops are tier 1."),
                    session.gameState()
            );
            return true;
        }
        return false;
    }

    private void openWorkerPage(Player player, PlayerSession session, CitizenJobType workerType) {
        UiScreenKey pageType = workerType == CitizenJobType.SOLDIER || workerType == CitizenJobType.TRAINEE
                ? UiScreenKey.CASTLE_TROOPS
                : UiScreenKey.CASTLE_CITIZENS;
        uiNavigator.open(
                pageType,
                player,
                new UiNavigationContext(player.getUuid(), player.getDisplayName()).withFeedback(workerFeedback(workerType)),
                session.gameState()
        );
    }

    private String workerFeedback(CitizenJobType workerType) {
        return workerType.name() + " anchor selected. This stationary NPC remains while task copies leave through the portal.";
    }
}
