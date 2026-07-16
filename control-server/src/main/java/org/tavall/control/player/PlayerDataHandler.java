package org.tavall.control.player;
import org.tavall.control.interior.InteriorTourMarkerHandler;
import org.tavall.control.population.PopulationDisplayGateway;
import org.tavall.control.player.PlayerGameStateHandler;
import org.tavall.control.player.PlayerSessionStore;
import org.tavall.control.player.PlayerSession;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.castle.ICastleSpawnHandler;
import org.tavall.control.player.IIpHashHandler;
import org.tavall.control.interior.IInteriorInstanceHandler;
import org.tavall.control.clock.IKingdomClockHandler;
import org.tavall.control.castle.ICastleBuildingVisualHandler;
import org.tavall.control.player.IPlayerDataHandler;
import org.tavall.control.player.IPlayerGameStateHandler;
import org.tavall.control.player.IPlayerProfileHandler;
import org.tavall.control.player.IPlayerSessionStore;
import org.tavall.control.resource.IResourceNodeVisualHandler;
import org.tavall.control.api.UIData;
import org.tavall.control.domain.CastleLocationData;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.PlayerProfile;
import com.tjxjnoobie.api.internal.utils.concurrent.AsyncTask;
import org.tavall.control.tasks.WorldTasks;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Orchestrates player initialization and persistence.
 */
public final class PlayerDataHandler implements IPlayerDataHandler, IDependencyInjectableConcrete {
    private static final Logger LOGGER = Logger.getLogger(PlayerDataHandler.class.getName());

    private final IPlayerProfileHandler profileHandler;
    private final IPlayerGameStateHandler gameStateHandler;
    private final IPlayerSessionStore sessionStore;
    private final ICastleSpawnHandler castleSpawnHandler;
    private final IInteriorInstanceHandler interiorInstanceHandler;
    private final IIpHashHandler ipHashHandler;
    private final IKingdomClockHandler clockHandler;
    private final IResourceNodeVisualHandler resourceNodeVisualHandler;
    private final ICastleBuildingVisualHandler buildingVisualHandler;
    private final PopulationDisplayGateway populationDisplayGateway;
    private final InteriorTourMarkerHandler interiorTourMarkerHandler;
    private final UIData uiNavigator;

    public PlayerDataHandler(
            IPlayerProfileHandler profileHandler,
            IPlayerGameStateHandler gameStateHandler,
            IPlayerSessionStore sessionStore,
            ICastleSpawnHandler castleSpawnHandler,
            IInteriorInstanceHandler interiorInstanceHandler,
            IIpHashHandler ipHashHandler,
            IKingdomClockHandler clockHandler,
            IResourceNodeVisualHandler resourceNodeVisualHandler,
            ICastleBuildingVisualHandler buildingVisualHandler,
            PopulationDisplayGateway populationDisplayGateway,
            InteriorTourMarkerHandler interiorTourMarkerHandler,
            UIData uiNavigator
    ) {
        this.profileHandler = Objects.requireNonNull(profileHandler, "profileHandler");
        this.gameStateHandler = Objects.requireNonNull(gameStateHandler, "gameStateHandler");
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.castleSpawnHandler = Objects.requireNonNull(castleSpawnHandler, "castleSpawnHandler");
        this.interiorInstanceHandler = Objects.requireNonNull(interiorInstanceHandler, "interiorInstanceHandler");
        this.ipHashHandler = Objects.requireNonNull(ipHashHandler, "ipHashHandler");
        this.clockHandler = Objects.requireNonNull(clockHandler, "clockHandler");
        this.resourceNodeVisualHandler = Objects.requireNonNull(resourceNodeVisualHandler, "resourceNodeVisualHandler");
        this.buildingVisualHandler = Objects.requireNonNull(buildingVisualHandler, "buildingVisualHandler");
        this.populationDisplayGateway = Objects.requireNonNull(populationDisplayGateway, "populationDisplayGateway");
        this.interiorTourMarkerHandler = Objects.requireNonNull(interiorTourMarkerHandler, "interiorTourMarkerHandler");
        this.uiNavigator = Objects.requireNonNull(uiNavigator, "uiNavigator");
    }

    public void handlePlayerReady(PlayerReadyEvent event) {
        ensureSession(event.getPlayer()).whenComplete((session, throwable) -> {
            if (throwable != null) {
                LOGGER.log(
                        Level.SEVERE,
                        "Failed to initialize session for "
                                + event.getPlayer().getDisplayName()
                                + " ("
                                + event.getPlayer().getUuid()
                                + ").",
                        throwable
                );
            }
        });
    }

    public void handlePlayerDisconnect(PlayerDisconnectEvent event) {
        try {
            if (event == null || event.getPlayerRef() == null) {
                return;
            }
            UUID playerId = event.getPlayerRef().getUuid();
            Ref<EntityStore> ref = event.getPlayerRef().getReference();
            LOGGER.log(
                    Level.INFO,
                    "Disconnect cleanup begin for "
                            + playerId
                            + " on thread "
                            + Thread.currentThread().getName()
                            + ". refValid="
                            + (ref != null && ref.isValid())
            );
            uiNavigator.clearTrackedPage(playerId);
            handlePlayerDisconnect(playerId);
        } catch (Throwable throwable) {
            LOGGER.log(Level.SEVERE, "Player disconnect handler failed.", throwable);
        }
    }

    void handlePlayerDisconnect(UUID playerId) {
        try {
            if (playerId == null) {
                return;
            }
            PlayerSession session = sessionStore.get(playerId);
            if (session == null) {
                return;
            }
            sessionStore.remove(playerId);
            uiNavigator.clearTrackedPage(playerId);
            PlayerProfile profile = session.profile();
            Instant now = Instant.now();
            PlayerGameState persistedState = session.gameState();
            if (persistedState != null && persistedState.interiorSession() != null) {
                persistedState = persistedState.withInteriorSession(null, now);
            }
            populationDisplayGateway.clearDisplays(playerId);
            interiorTourMarkerHandler.clearTourMarkers(playerId);
            PlayerGameState finalPersistedState = persistedState;
            AsyncTask.runAsync(() -> {
                profileHandler.persist(profile, now);
                gameStateHandler.persistState(finalPersistedState, now);
            });
            interiorInstanceHandler.releaseInteriorWorld(playerId);
            LOGGER.log(Level.INFO, "Disconnect cleanup complete for " + playerId + ".");
        } catch (Throwable throwable) {
            LOGGER.log(Level.SEVERE, "Player disconnect cleanup failed for " + playerId + ".", throwable);
        }
    }

    public CompletableFuture<PlayerSession> ensureSession(Player player) {
        PlayerSession existing = sessionStore.get(player.getUuid());
        if (existing != null) {
            return CompletableFuture.completedFuture(existing);
        }
        UUID playerId = player.getUuid();
        String displayName = player.getDisplayName();

        return resolveSpawnLocation(player)
                .thenCompose(spawnLocation -> AsyncTask.supplyAsync(() -> initializeSession(playerId, displayName, spawnLocation)))
                .thenCompose(session -> {
                    if (session == null) {
                        return CompletableFuture.completedFuture(null);
                    }
                    CompletableFuture<PlayerSession> readyFuture = new CompletableFuture<>();
                    if (player.getWorld() == null) {
                        sessionStore.put(session);
                        LOGGER.log(
                                Level.WARNING,
                                "Session initialized for " + displayName + " (" + playerId + ") without a live world reference."
                        );
                        readyFuture.complete(session);
                        return readyFuture;
                    }
                    WorldTasks.executeSafe(player.getWorld(), "PlayerDataHandler.ensureSession", () -> {
                        try {
                            sessionStore.put(session);
                            castleSpawnHandler.ensureCastleSpawned(player, session.gameState().castleLocation());
                            resourceNodeVisualHandler.ensureNodes(playerId, session.gameState());
                            buildingVisualHandler.ensureBuildings(playerId, session.gameState());
                            clockHandler.applyToWorld(player.getWorld());
                            LOGGER.log(Level.INFO, "Session initialized for " + displayName + " (" + playerId + ").");
                            readyFuture.complete(session);
                        } catch (Throwable throwable) {
                            readyFuture.completeExceptionally(throwable);
                        }
                    });
                    return readyFuture;
                });
    }

    private PlayerSession initializeSession(UUID playerId, String displayName, CastleLocationData spawnLocation) {
        Instant now = Instant.now();
        String timezone = clockHandler.snapshot().timezone();
        String ipHash = ipHashHandler.hash(null);
        PlayerProfile profile = profileHandler.loadOrCreate(playerId, displayName, timezone, ipHash, now);
        PlayerGameState state = gameStateHandler.loadOrCreate(profile.id(), playerId, spawnLocation, now);
        return new PlayerSession(playerId, profile, state);
    }

    private CompletableFuture<CastleLocationData> resolveSpawnLocation(Player player) {
        if (player == null || player.getWorld() == null) {
            return CompletableFuture.completedFuture(new CastleLocationData("default", 0.0D, 80.0D, 0.0D));
        }

        CompletableFuture<CastleLocationData> future = new CompletableFuture<>();
        WorldTasks.executeSafe(player.getWorld(), "PlayerDataHandler.resolveSpawnLocation", () -> {
            try {
                TransformComponent transform = player.getTransformComponent();
                Vector3d position = transform == null ? null : transform.getPosition();
                if (position == null) {
                    future.complete(new CastleLocationData(player.getWorld().getName(), 0.0D, 80.0D, 0.0D));
                    return;
                }

                double x = position.getX();
                double y = position.getY();
                double z = position.getZ();
                if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z) || y < 5.0D) {
                    future.complete(new CastleLocationData(player.getWorld().getName(), 0.0D, 80.0D, 0.0D));
                    return;
                }

                future.complete(new CastleLocationData(player.getWorld().getName(), x, y + 1.0D, z));
            } catch (Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        });
        return future;
    }

}
