package org.tavall.control.interior;
import org.tavall.control.interior.InteriorTourMarkerHandler;
import org.tavall.control.population.PopulationDisplayGateway;
import org.tavall.control.player.PlayerGameStateHandler;
import org.tavall.control.player.PlayerSessionStore;
import org.tavall.control.player.PlayerSession;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.castle.ICastleBuildingVisualHandler;
import org.tavall.control.interior.IInteriorInstanceHandler;
import org.tavall.control.interior.IInteriorWorldHandler;
import org.tavall.control.player.IPlayerGameStateHandler;
import org.tavall.control.player.IPlayerSessionStore;
import org.tavall.control.player.IPlayerTeleportHandler;
import org.tavall.control.ui.IUiNavigator;
import org.tavall.control.domain.CastleLocationData;
import org.tavall.control.domain.InteriorSessionData;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.UiNavigationContext;
import org.tavall.control.interior.InteriorLayout;
import org.tavall.control.interior.InteriorLayoutHandler;
import org.tavall.control.interior.InteriorStructureHandler;
import com.tjxjnoobie.api.internal.utils.concurrent.AsyncTask;
import org.tavall.control.tasks.WorldTasks;
import org.tavall.control.ui.UiPageType;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Handles interior transitions within the same server instance.
 */
public final class InteriorWorldHandler implements IInteriorWorldHandler, IDependencyInjectableConcrete {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final Duration TRANSITION_TIMEOUT = Duration.ofSeconds(20L);
    private static final long PAGE_CLOSE_BUFFER_MILLIS = 250L;
    private static final long EXIT_UI_DELAY_MILLIS = 750L;
    private static final long INTERIOR_READY_RETRY_MILLIS = 250L;
    private static final int INTERIOR_READY_MAX_RETRIES = 48;
    private static final int EXIT_READY_MAX_RETRIES = 48;

    private final AtomicLong transitionSequence = new AtomicLong();
    private final ConcurrentHashMap<UUID, TransitionToken> transitionStates = new ConcurrentHashMap<>();
    private final IPlayerSessionStore sessionStore;
    private final IPlayerGameStateHandler gameStateHandler;
    private final IInteriorInstanceHandler interiorInstanceHandler;
    private final InteriorLayoutHandler layoutHandler;
    private final InteriorStructureHandler structureHandler;
    private final InteriorTourMarkerHandler interiorTourMarkerHandler;
    private final IPlayerTeleportHandler playerTeleportHandler;
    private final PopulationDisplayGateway displayHandler;
    private final ICastleBuildingVisualHandler buildingVisualHandler;
    private final IUiNavigator uiNavigator;

    public InteriorWorldHandler(
            IPlayerSessionStore sessionStore,
            IPlayerGameStateHandler gameStateHandler,
            IInteriorInstanceHandler interiorInstanceHandler,
            InteriorLayoutHandler layoutHandler,
            InteriorStructureHandler structureHandler,
            InteriorTourMarkerHandler interiorTourMarkerHandler,
            IPlayerTeleportHandler playerTeleportHandler,
            PopulationDisplayGateway displayHandler,
            ICastleBuildingVisualHandler buildingVisualHandler,
            IUiNavigator uiNavigator
    ) {
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.gameStateHandler = Objects.requireNonNull(gameStateHandler, "gameStateHandler");
        this.interiorInstanceHandler = Objects.requireNonNull(interiorInstanceHandler, "interiorInstanceHandler");
        this.layoutHandler = Objects.requireNonNull(layoutHandler, "layoutHandler");
        this.structureHandler = Objects.requireNonNull(structureHandler, "structureHandler");
        this.interiorTourMarkerHandler = Objects.requireNonNull(interiorTourMarkerHandler, "interiorTourMarkerHandler");
        this.playerTeleportHandler = Objects.requireNonNull(playerTeleportHandler, "playerTeleportHandler");
        this.displayHandler = Objects.requireNonNull(displayHandler, "displayHandler");
        this.buildingVisualHandler = Objects.requireNonNull(buildingVisualHandler, "buildingVisualHandler");
        this.uiNavigator = Objects.requireNonNull(uiNavigator, "uiNavigator");
    }

    public void enterInterior(Player player) {
        UUID playerId = player.getUuid();
        PlayerSession session = sessionStore.get(playerId);
        if (session == null) {
            LOGGER.at(Level.WARNING).log("Interior entry ignored for %s because no session is available.", playerId);
            return;
        }
        PlayerGameState state = session.gameState();
        CastleLocationData castleLocation = state.castleLocation();
        if (castleLocation == null) {
            LOGGER.at(Level.WARNING).log("Interior entry ignored for %s because the castle location is missing.", playerId);
            return;
        }
        if (!beginTransition(playerId, TransitionKind.ENTER)) {
            LOGGER.at(Level.INFO).log("Interior entry ignored for %s because a transition is already in progress.", playerId);
            return;
        }
        TransitionToken transitionToken = transitionStates.get(playerId);
        withLivePlayer(playerId, (livePlayer) -> {
            if (!isActiveTransition(playerId, transitionToken)) {
                return;
            }
            closeCurrentPage(livePlayer);
            uiNavigator.clearTrackedPage(playerId);
            if (livePlayer.getWorld() == null) {
                LOGGER.at(Level.WARNING).log("Interior entry skipped because player %s is no longer available.", playerId);
                completeTransition(playerId, transitionToken);
                return;
            }
            LOGGER.at(Level.INFO).log("Beginning interior entry for %s (%s).", livePlayer.getDisplayName(), playerId);
            beginInteriorEntry(livePlayer, session, state, castleLocation, transitionToken);
        });
    }

    private void beginInteriorEntry(
            Player player,
            PlayerSession session,
            PlayerGameState state,
            CastleLocationData castleLocation,
            TransitionToken transitionToken
    ) {
        UUID playerId = player.getUuid();
        int interiorIndex = gameStateHandler.interiorInstanceIndex(state);
        InteriorLayout layout = layoutHandler.createLayoutForCastle(castleLocation, interiorIndex);
        Vector3d entryPosition = interiorEntryPosition(player, layout);
        Instant now = Instant.now();
        boolean firstInteriorTutorialPending = gameStateHandler.isInteriorTutorialPending(state);
        boolean firstInteriorTourPending = gameStateHandler.isInteriorTourPending(state);
        PlayerGameState tutorialState = state;
        if (firstInteriorTutorialPending) {
            tutorialState = gameStateHandler.markInteriorTutorialSeen(tutorialState, now);
        }
        if (firstInteriorTourPending) {
            tutorialState = gameStateHandler.markInteriorTourSeen(tutorialState, now);
        }
        PlayerGameState finalTutorialState = tutorialState;
        CompletableFuture<World> interiorWorldFuture = interiorInstanceHandler.resolveInteriorWorld(playerId);
        interiorWorldFuture.whenComplete((interiorWorld, throwable) -> {
            if (!isActiveTransition(playerId, transitionToken)) {
                return;
            }
            if (throwable != null) {
                Throwable rootCause = rootCause(throwable);
                LOGGER.at(Level.SEVERE).withCause(rootCause).log(
                        "Interior world resolve failed for %s (%s).",
                        player.getDisplayName(),
                        playerId
                );
                sendPlayerMessage(playerId, Message.raw("Interior transfer failed: " + safeMessage(rootCause)).color("red"));
                completeTransition(playerId, transitionToken);
                return;
            }
            if (interiorWorld == null) {
                LOGGER.at(Level.WARNING).log("Interior entry failed because no interior world was resolved for %s.", playerId);
                sendPlayerMessage(playerId, Message.raw("Interior transfer failed: no interior world available.").color("red"));
                completeTransition(playerId, transitionToken);
                return;
            }

            InteriorSessionData interiorSession = new InteriorSessionData(
                    interiorWorld.getName(),
                    castleLocation,
                    now
            );
            PlayerGameState updated = finalTutorialState.withInteriorSession(interiorSession, now);
            PlayerSession updatedSession = sessionStore.get(playerId);
            if (updatedSession != null) {
                updatedSession.updateGameState(updated);
            } else {
                session.updateGameState(updated);
            }
            gameStateHandler.cacheState(playerId, updated);
            AsyncTask.runAsync(() -> gameStateHandler.persistState(updated, now));

            preloadInteriorChunks(interiorWorld, layout).whenComplete((ignored, preloadThrowable) -> {
                if (!isActiveTransition(playerId, transitionToken)) {
                    return;
                }
                if (preloadThrowable != null) {
                    Throwable rootCause = rootCause(preloadThrowable);
                    LOGGER.at(Level.SEVERE).withCause(rootCause).log(
                            "Interior chunk preload failed for %s (%s).",
                            player.getDisplayName(),
                            playerId
                    );
                    sendPlayerMessage(playerId, Message.raw("Interior transfer failed: " + safeMessage(rootCause)).color("red"));
                    completeTransition(playerId, transitionToken);
                    return;
                }
                try {
                    UiNavigationContext context = prepareInteriorWorld(
                            interiorWorld,
                            player,
                            updated,
                            layout,
                            entryPosition,
                            firstInteriorTutorialPending,
                            firstInteriorTourPending
                    );
                    waitForInteriorReady(
                            playerId,
                            interiorWorld,
                            entryPosition,
                            updated,
                            context,
                            layout,
                            firstInteriorTourPending,
                            INTERIOR_READY_MAX_RETRIES,
                            transitionToken
                    );
                } catch (Throwable exception) {
                    Throwable rootCause = rootCause(exception);
                    LOGGER.at(Level.SEVERE).withCause(rootCause).log(
                            "Interior entry failed for %s (%s).",
                            player.getDisplayName(),
                            playerId
                    );
                    sendPlayerMessage(playerId, Message.raw("Interior transfer failed: " + safeMessage(rootCause)).color("red"));
                    completeTransition(playerId, transitionToken);
                }
            });
        });
    }

    @Override
    public void generateInterior(Player player) {
        if (player == null) {
            return;
        }
        UUID playerId = player.getUuid();
        PlayerSession session = sessionStore.get(playerId);
        if (session == null) {
            player.sendMessage(Message.raw("Interior generate skipped: player session missing.").color("red"));
            return;
        }
        if (session.gameState().castleLocation() == null) {
            player.sendMessage(Message.raw("Interior generate skipped: castle location missing.").color("red"));
            return;
        }
        if (session.gameState().interiorSession() != null) {
            rebuildInterior(player);
            return;
        }
        enterInterior(player);
    }

    @Override
    public void rebuildInterior(Player player) {
        if (player == null) {
            return;
        }
        UUID playerId = player.getUuid();
        PlayerSession session = sessionStore.get(playerId);
        if (session == null) {
            return;
        }
        PlayerGameState state = session.gameState();
        CastleLocationData castleLocation = state.castleLocation();
        if (castleLocation == null) {
            player.sendMessage(Message.raw("Interior rebuild skipped: castle location missing.").color("red"));
            return;
        }

        int interiorIndex = gameStateHandler.interiorInstanceIndex(state);
        InteriorLayout targetLayout = layoutHandler.createLayoutForCastle(castleLocation, interiorIndex);
        InteriorLayout previousLayout = state.interiorSession() != null && state.interiorSession().returnLocation() != null
                ? layoutHandler.createLayoutForCastle(state.interiorSession().returnLocation(), interiorIndex)
                : targetLayout;
        Vector3d entryPosition = interiorEntryPosition(player, targetLayout);
        boolean tourPending = gameStateHandler.isInteriorTourPending(state);

        interiorInstanceHandler.resolveInteriorWorld(playerId).whenComplete((world, throwable) -> {
            if (throwable != null || world == null) {
                Throwable rootCause = rootCause(throwable == null ? new IllegalStateException("missing interior world") : throwable);
                LOGGER.at(Level.WARNING).withCause(rootCause).log("Interior rebuild failed for %s.", playerId);
                sendPlayerMessage(playerId, Message.raw("Interior rebuild failed: " + safeMessage(rootCause)).color("red"));
                return;
            }
            preloadInteriorChunks(world, targetLayout).whenComplete((ignored, preloadThrowable) -> {
                if (preloadThrowable != null) {
                    Throwable rootCause = rootCause(preloadThrowable);
                    LOGGER.at(Level.WARNING).withCause(rootCause).log("Interior rebuild chunk preload failed for %s.", playerId);
                    sendPlayerMessage(playerId, Message.raw("Interior rebuild failed: " + safeMessage(rootCause)).color("red"));
                    return;
                }
                WorldTasks.executeSafe(world, "InteriorWorldHandler.rebuildInterior", () -> {
                    try {
                        structureHandler.clearStructure(world, previousLayout);
                    } catch (Throwable ignoredError) {
                    }
                    safeEnsureStructure(world, targetLayout, playerId);
                    interiorTourMarkerHandler.clearTourMarkers(playerId);
                    displayHandler.clearDisplays(playerId);
                    safeEnsureTourMarkers(world, targetLayout, playerId, tourPending);
                    safeEnsurePopulationDisplays(world, targetLayout, playerId, state);
                    buildingVisualHandler.refreshBuildings(playerId, state);
                });
            });
            withLivePlayer(playerId, (livePlayer) -> {
                if (livePlayer.getWorld() != null && livePlayer.getWorld().getName().equals(world.getName())) {
                    safeTeleportToInterior(livePlayer, world, entryPosition);
                    livePlayer.sendMessage(Message.raw("Interior rebuilt.").color("green"));
                    uiNavigator.open(UiPageType.INTERIOR_MAIN, livePlayer, new UiNavigationContext(playerId, livePlayer.getDisplayName()), state);
                }
            });
        });
    }

    @Override
    public void deleteInterior(Player player) {
        if (player == null) {
            return;
        }
        UUID playerId = player.getUuid();
        PlayerSession session = sessionStore.get(playerId);
        if (session == null) {
            player.sendMessage(Message.raw("Interior delete skipped: player session missing.").color("red"));
            return;
        }
        PlayerGameState state = session.gameState();
        CastleLocationData castleLocation = state.castleLocation();
        int previousIndex = gameStateHandler.interiorInstanceIndex(state);
        InteriorLayout previousLayout = castleLocation == null
                ? null
                : layoutHandler.createLayoutForCastle(castleLocation, previousIndex);
        InteriorSessionData previousInteriorSession = state.interiorSession();
        Instant now = Instant.now();
        PlayerGameState updatedState = gameStateHandler.bumpInteriorInstanceIndex(state.withInteriorSession(null, now), now);
        session.updateGameState(updatedState);
        gameStateHandler.cacheState(playerId, updatedState);
        AsyncTask.runAsync(() -> gameStateHandler.persistState(updatedState, now));

        interiorTourMarkerHandler.clearTourMarkers(playerId);
        displayHandler.clearDisplays(playerId);
        buildingVisualHandler.clearBuildings(playerId);
        if (previousInteriorSession != null && previousLayout != null) {
            World previousWorld = com.hypixel.hytale.server.core.universe.Universe.get().getWorld(previousInteriorSession.interiorWorldName());
            if (previousWorld != null) {
                WorldTasks.executeSafe(previousWorld, "InteriorWorldHandler.deleteInterior.clearStructure", () -> {
                    try {
                        structureHandler.clearStructure(previousWorld, previousLayout);
                    } catch (Throwable throwable) {
                        LOGGER.at(Level.WARNING).withCause(throwable).log("Interior delete structure cleanup failed for %s.", playerId);
                    }
                });
            }
        }
        interiorInstanceHandler.releaseInteriorWorld(playerId);
        if (previousInteriorSession != null && castleLocation != null && player.getWorld() != null
                && previousInteriorSession.interiorWorldName().equals(player.getWorld().getName())) {
            World returnWorld = com.hypixel.hytale.server.core.universe.Universe.get().getWorld(castleLocation.worldName());
            if (returnWorld != null) {
                Vector3d returnPosition = playerTeleportHandler.standingPosition(player, castleLocation.standingBaseVector());
                WorldTasks.executeSafe(returnWorld, "InteriorWorldHandler.deleteInterior.returnPlayer", () ->
                        playerTeleportHandler.teleport(player, returnWorld, returnPosition));
            }
        }
        player.sendMessage(Message.raw("Interior deleted. Next entry will generate a fresh instance.").color("green"));
    }

    @Override
    public void moveInterior(Player player) {
        if (player == null) {
            return;
        }
        UUID playerId = player.getUuid();
        PlayerSession session = sessionStore.get(playerId);
        if (session == null) {
            return;
        }
        PlayerGameState state = session.gameState();
        CastleLocationData castleLocation = state.castleLocation();
        if (castleLocation == null) {
            player.sendMessage(Message.raw("Interior move skipped: castle location missing.").color("red"));
            return;
        }

        int previousIndex = gameStateHandler.interiorInstanceIndex(state);
        InteriorLayout previousLayout = layoutHandler.createLayoutForCastle(castleLocation, previousIndex);

        Instant now = Instant.now();
        PlayerGameState updatedState = gameStateHandler.bumpInteriorInstanceIndex(state, now);
        session.updateGameState(updatedState);
        gameStateHandler.cacheState(playerId, updatedState);
        AsyncTask.runAsync(() -> gameStateHandler.persistState(updatedState, now));

        int nextIndex = gameStateHandler.interiorInstanceIndex(updatedState);
        InteriorLayout targetLayout = layoutHandler.createLayoutForCastle(castleLocation, nextIndex);
        Vector3d entryPosition = interiorEntryPosition(player, targetLayout);
        boolean tourPending = gameStateHandler.isInteriorTourPending(updatedState);

        interiorInstanceHandler.resolveInteriorWorld(playerId).whenComplete((world, throwable) -> {
            if (throwable != null || world == null) {
                Throwable rootCause = rootCause(throwable == null ? new IllegalStateException("missing interior world") : throwable);
                LOGGER.at(Level.WARNING).withCause(rootCause).log("Interior move failed for %s.", playerId);
                sendPlayerMessage(playerId, Message.raw("Interior move failed: " + safeMessage(rootCause)).color("red"));
                return;
            }
            preloadInteriorChunks(world, targetLayout).whenComplete((ignored, preloadThrowable) -> {
                if (preloadThrowable != null) {
                    Throwable rootCause = rootCause(preloadThrowable);
                    LOGGER.at(Level.WARNING).withCause(rootCause).log("Interior move chunk preload failed for %s.", playerId);
                    sendPlayerMessage(playerId, Message.raw("Interior move failed: " + safeMessage(rootCause)).color("red"));
                    return;
                }
                WorldTasks.executeSafe(world, "InteriorWorldHandler.moveInterior", () -> {
                    try {
                        structureHandler.clearStructure(world, previousLayout);
                    } catch (Throwable ignoredError) {
                    }
                    safeEnsureStructure(world, targetLayout, playerId);
                    interiorTourMarkerHandler.clearTourMarkers(playerId);
                    displayHandler.clearDisplays(playerId);
                    safeEnsureTourMarkers(world, targetLayout, playerId, tourPending);
                    safeEnsurePopulationDisplays(world, targetLayout, playerId, updatedState);
                    buildingVisualHandler.refreshBuildings(playerId, updatedState);
                });
            });

            withLivePlayer(playerId, (livePlayer) -> {
                if (livePlayer.getWorld() != null && livePlayer.getWorld().getName().equals(world.getName())) {
                    safeTeleportToInterior(livePlayer, world, entryPosition);
                    livePlayer.sendMessage(Message.raw("Interior moved.").color("green"));
                    uiNavigator.open(UiPageType.INTERIOR_MAIN, livePlayer, new UiNavigationContext(playerId, livePlayer.getDisplayName()), updatedState);
                    return;
                }
                livePlayer.sendMessage(Message.raw("Interior moved. Enter /kd interior to visit the new instance.").color("green"));
            });
        });
    }

    public void exitInterior(Player player) {
        UUID playerId = player.getUuid();
        PlayerSession session = sessionStore.get(playerId);
        if (session == null) {
            return;
        }
        PlayerGameState state = session.gameState();
        InteriorSessionData interiorSession = state.interiorSession();
        if (interiorSession == null) {
            LOGGER.at(Level.INFO).log("Interior exit ignored for %s because no active interior session exists.", playerId);
            return;
        }
        if (!beginTransition(playerId, TransitionKind.EXIT)) {
            LOGGER.at(Level.INFO).log("Interior exit ignored for %s because a transition is already in progress.", playerId);
            return;
        }
        TransitionToken transitionToken = transitionStates.get(playerId);
        LOGGER.at(Level.INFO).log("Beginning interior exit for %s (%s).", player.getDisplayName(), playerId);
        World transitionWorld = player.getWorld();
        closeCurrentPage(player);
        uiNavigator.clearTrackedPage(playerId);
        if (transitionWorld != null) {
            WorldTasks.executeSafe(transitionWorld, "InteriorWorldHandler.exitInterior.clearAnchors", () -> {
                interiorTourMarkerHandler.clearTourMarkers(playerId);
                displayHandler.clearDisplays(playerId);
            });
        }
        CastleLocationData returnLocation = interiorSession.returnLocation();
        PlayerGameState updated = state.withInteriorSession(null, Instant.now());
        session.updateGameState(updated);
        HytaleServer.SCHEDULED_EXECUTOR.schedule(
                () -> executeInteriorExitOnWorld(transitionWorld, playerId, returnLocation, updated, transitionToken),
                PAGE_CLOSE_BUFFER_MILLIS,
                TimeUnit.MILLISECONDS
        );
        gameStateHandler.cacheState(playerId, updated);
        AsyncTask.runAsync(() -> gameStateHandler.persistState(updated, Instant.now()));
    }

    private void executeInteriorExitOnWorld(
            World transitionWorld,
            UUID playerId,
            CastleLocationData returnLocation,
            PlayerGameState updated,
            TransitionToken transitionToken
    ) {
        if (!isActiveTransition(playerId, transitionToken)) {
            return;
        }
        if (transitionWorld == null) {
            LOGGER.at(Level.WARNING).log("Interior exit skipped for %s because the transition world is not available.", playerId);
            completeTransition(playerId, transitionToken);
            return;
        }
        WorldTasks.executeSafe(
                transitionWorld,
                "InteriorWorldHandler.executeInteriorExitOnWorld",
                () -> completeInteriorExit(transitionWorld, playerId, returnLocation, updated, transitionToken)
        );
    }

    private void completeInteriorExit(
            World transitionWorld,
            UUID playerId,
            CastleLocationData returnLocation,
            PlayerGameState updated,
            TransitionToken transitionToken
    ) {
        if (!isActiveTransition(playerId, transitionToken)) {
            return;
        }
        World uiWorld = transitionWorld;
        try {
            var returnWorld = com.hypixel.hytale.server.core.universe.Universe.get().getWorld(returnLocation.worldName());
            if (returnWorld == null) {
                LOGGER.at(Level.WARNING).log(
                        "Interior exit return world %s is not available for %s; castle UI will still reopen.",
                        returnLocation.worldName(),
                        playerId
                );
                completeTransition(playerId, transitionToken);
                return;
            }
            uiWorld = returnWorld;
            Vector3d returnPosition = new Vector3d(returnLocation.x(), returnLocation.y(), returnLocation.z());
            withLivePlayer(playerId, (livePlayer) -> safelyMovePlayerToReturnLocation(livePlayer, returnWorld, returnPosition));
        } catch (Throwable throwable) {
            Throwable rootCause = rootCause(throwable);
            LOGGER.at(Level.WARNING).withCause(throwable).log(
                    "Interior exit completion failed for %s; castle UI will still attempt to reopen. cause=%s: %s",
                    playerId,
                    rootCause.getClass().getName(),
                    safeMessage(rootCause)
            );
        } finally {
            scheduleCastleUiAfterExit(uiWorld == null ? null : uiWorld.getName(), playerId, updated, transitionToken);
            scheduleInteriorRelease(playerId);
        }
    }

    private void scheduleCastleUiAfterExit(
            String expectedWorldName,
            UUID playerId,
            PlayerGameState updated,
            TransitionToken transitionToken
    ) {
        HytaleServer.SCHEDULED_EXECUTOR.schedule(
                () -> {
                    if (!isActiveTransition(playerId, transitionToken)) {
                        return;
                    }
                    waitForCastleUiAfterExit(playerId, expectedWorldName, updated, EXIT_READY_MAX_RETRIES, transitionToken);
                },
                EXIT_UI_DELAY_MILLIS,
                TimeUnit.MILLISECONDS
        );
    }

    private void waitForCastleUiAfterExit(
            UUID playerId,
            String expectedWorldName,
            PlayerGameState updated,
            int retriesRemaining,
            TransitionToken transitionToken
    ) {
        if (!isActiveTransition(playerId, transitionToken)) {
            return;
        }
        if (expectedWorldName == null || expectedWorldName.isBlank()) {
            if (retriesRemaining <= 0) {
                LOGGER.at(Level.WARNING).log(
                        "Interior exit UI reopen timed out for %s waiting for world %s.",
                        playerId,
                        expectedWorldName
                );
                completeTransition(playerId, transitionToken);
                return;
            }
            retryCastleUiAfterExit(playerId, expectedWorldName, updated, retriesRemaining - 1, transitionToken);
            return;
        }
        World expectedWorld = com.hypixel.hytale.server.core.universe.Universe.get().getWorld(expectedWorldName);
        if (expectedWorld == null) {
            if (retriesRemaining <= 0) {
                LOGGER.at(Level.WARNING).log(
                        "Interior exit UI reopen timed out for %s because world %s is unavailable.",
                        playerId,
                        expectedWorldName
                );
                completeTransition(playerId, transitionToken);
                return;
            }
            retryCastleUiAfterExit(playerId, expectedWorldName, updated, retriesRemaining - 1, transitionToken);
            return;
        }

        WorldTasks.executeSafe(expectedWorld, "InteriorWorldHandler.waitForCastleUiAfterExit", () -> {
            if (!isActiveTransition(playerId, transitionToken)) {
                return;
            }
            Ref<EntityStore> playerRef = expectedWorld.getEntityRef(playerId);
            if (playerRef == null || !playerRef.isValid()) {
                if (retriesRemaining <= 0) {
                    LOGGER.at(Level.WARNING).log(
                            "Interior exit UI reopen timed out for %s after returning to world %s.",
                            playerId,
                            expectedWorldName
                    );
                    completeTransition(playerId, transitionToken);
                    return;
                }
                retryCastleUiAfterExit(playerId, expectedWorldName, updated, retriesRemaining - 1, transitionToken);
                return;
            }
            Store<EntityStore> store = playerRef.getStore();
            Player livePlayer = store.getComponent(playerRef, Player.getComponentType());
            if (livePlayer == null || livePlayer.getWorld() == null
                    || (expectedWorldName != null && !expectedWorldName.equals(livePlayer.getWorld().getName()))) {
                if (retriesRemaining <= 0) {
                    LOGGER.at(Level.WARNING).log(
                            "Interior exit UI reopen timed out for %s after returning to world %s.",
                            playerId,
                            expectedWorldName
                    );
                    completeTransition(playerId, transitionToken);
                    return;
                }
                retryCastleUiAfterExit(playerId, expectedWorldName, updated, retriesRemaining - 1, transitionToken);
                return;
            }
            try {
                LOGGER.at(Level.INFO).log("Interior exit complete for %s; opening castle UI.", livePlayer.getDisplayName());
                completeTransition(playerId, transitionToken);
                uiNavigator.open(
                        UiPageType.CASTLE_MAIN,
                        livePlayer,
                        new UiNavigationContext(playerId, livePlayer.getDisplayName()),
                        updated
                );
            } catch (Throwable throwable) {
                Throwable rootCause = rootCause(throwable);
                LOGGER.at(Level.SEVERE).withCause(throwable).log(
                        "Interior exit UI reopen failed for %s. cause=%s: %s",
                        playerId,
                        rootCause.getClass().getName(),
                        safeMessage(rootCause)
                );
                completeTransition(playerId, transitionToken);
            }
        });
    }

    private void retryCastleUiAfterExit(
            UUID playerId,
            String expectedWorldName,
            PlayerGameState updated,
            int retriesRemaining,
            TransitionToken transitionToken
    ) {
        if (!isActiveTransition(playerId, transitionToken)) {
            return;
        }
        HytaleServer.SCHEDULED_EXECUTOR.schedule(
                () -> waitForCastleUiAfterExit(playerId, expectedWorldName, updated, retriesRemaining, transitionToken),
                INTERIOR_READY_RETRY_MILLIS,
                TimeUnit.MILLISECONDS
        );
    }

    private void scheduleInteriorRelease(UUID playerId) {
        HytaleServer.SCHEDULED_EXECUTOR.schedule(
                () -> interiorInstanceHandler.releaseInteriorWorld(playerId),
                EXIT_UI_DELAY_MILLIS,
                TimeUnit.MILLISECONDS
        );
    }

    private void safelyMovePlayerToReturnLocation(
            Player livePlayer,
            com.hypixel.hytale.server.core.universe.world.World returnWorld,
            Vector3d returnPosition
    ) {
        try {
            if (livePlayer.getWorld() != null && livePlayer.getWorld().getName().equals(returnWorld.getName())) {
                playerTeleportHandler.moveWithoutTeleportAck(livePlayer, returnPosition);
            } else {
                WorldTasks.executeSafe(
                        returnWorld,
                        "InteriorWorldHandler.safelyMovePlayerToReturnLocation",
                        () -> playerTeleportHandler.teleport(livePlayer, returnWorld, returnPosition)
                );
            }
        } catch (Throwable throwable) {
            Throwable rootCause = rootCause(throwable);
            LOGGER.at(Level.WARNING).withCause(rootCause).log(
                    "Interior exit movement failed for %s into %s; castle UI will still reopen. cause=%s: %s",
                    livePlayer.getUuid(),
                    returnWorld.getName(),
                    rootCause.getClass().getName(),
                    safeMessage(rootCause)
            );
        }
    }

    private UiNavigationContext prepareInteriorWorld(
            com.hypixel.hytale.server.core.universe.world.World world,
            Player player,
            PlayerGameState updated,
            InteriorLayout layout,
            Vector3d entryPosition,
            boolean firstInteriorTutorialPending,
            boolean firstInteriorTourPending
    ) {
        WorldTasks.executeSafe(world, "InteriorWorldHandler.prepareInteriorWorld", () -> {
            safeEnsureStructure(world, layout, player.getUuid());
            safeTeleportToInterior(player, world, entryPosition);
        });
        String tutorialMessage = (firstInteriorTutorialPending || firstInteriorTourPending)
                ? "Step 1: follow the tour markers. Step 2: inspect the citizen and troop anchors. Step 3: leave through the exit lane when you are done."
                : "Interior tutorial complete: citizen and troop anchors stay here while the upgrade pipeline grows.";
        return new UiNavigationContext(player.getUuid(), player.getDisplayName(), tutorialMessage);
    }

    private void safeEnsureStructure(
            com.hypixel.hytale.server.core.universe.world.World world,
            InteriorLayout layout,
            UUID playerId
    ) {
        try {
            structureHandler.ensureStructure(world, layout);
        } catch (Throwable throwable) {
            LOGGER.at(Level.WARNING).withCause(throwable).log("Interior structure setup failed for %s.", playerId);
        }
    }

    private void safeEnsureTourMarkers(
            com.hypixel.hytale.server.core.universe.world.World world,
            InteriorLayout layout,
            UUID playerId,
            boolean firstInteriorTourPending
    ) {
        try {
            interiorTourMarkerHandler.ensureTourMarkers(playerId, world, layout, firstInteriorTourPending);
        } catch (Throwable throwable) {
            LOGGER.at(Level.WARNING).withCause(throwable).log("Interior tour marker setup failed for %s.", playerId);
        }
    }

    private void safeEnsurePopulationDisplays(
            com.hypixel.hytale.server.core.universe.world.World world,
            InteriorLayout layout,
            UUID playerId,
            PlayerGameState updated
    ) {
        try {
            displayHandler.ensureDisplays(playerId, world, layout, updated.populationSummary());
        } catch (Throwable throwable) {
            LOGGER.at(Level.WARNING).withCause(throwable).log("Interior population display setup failed for %s.", playerId);
        }
    }

    private void safeTeleportToInterior(
            Player player,
            com.hypixel.hytale.server.core.universe.world.World world,
            Vector3d entryPosition
    ) {
        try {
            if (player.getWorld() != null && player.getWorld().getName().equals(world.getName())) {
                playerTeleportHandler.moveWithoutTeleportAck(player, entryPosition);
                return;
            }
            playerTeleportHandler.teleport(player, world, entryPosition);
        } catch (Throwable throwable) {
            Throwable rootCause = rootCause(throwable);
            LOGGER.at(Level.WARNING).withCause(rootCause).log(
                    "Interior teleport scheduling failed for %s into %s. cause=%s: %s",
                    player.getUuid(),
                    world.getName(),
                    rootCause.getClass().getName(),
                    safeMessage(rootCause)
            );
        }
    }

    private void waitForInteriorReady(
            UUID playerId,
            com.hypixel.hytale.server.core.universe.world.World targetWorld,
            Vector3d entryPosition,
            PlayerGameState updatedState,
            UiNavigationContext context,
            InteriorLayout layout,
            boolean firstInteriorTourPending,
            int retriesRemaining,
            TransitionToken transitionToken
    ) {
        if (!isActiveTransition(playerId, transitionToken)) {
            return;
        }
        if (targetWorld == null) {
            if (retriesRemaining <= 0) {
                LOGGER.at(Level.WARNING).log("Interior entry timed out for %s because the target world is not available.", playerId);
                completeTransition(playerId, transitionToken);
                return;
            }
            HytaleServer.SCHEDULED_EXECUTOR.schedule(
                    () -> waitForInteriorReady(playerId, targetWorld, entryPosition, updatedState, context, layout, firstInteriorTourPending, retriesRemaining - 1, transitionToken),
                    INTERIOR_READY_RETRY_MILLIS,
                    TimeUnit.MILLISECONDS
            );
            return;
        }

        WorldTasks.executeSafe(targetWorld, "InteriorWorldHandler.waitForInteriorReady", () -> {
            if (!isActiveTransition(playerId, transitionToken)) {
                return;
            }
            Ref<EntityStore> playerRef = targetWorld.getEntityRef(playerId);
            if (playerRef == null || !playerRef.isValid()) {
                if (retriesRemaining <= 0) {
                    LOGGER.at(Level.WARNING).log("Interior entry timed out for %s before the live player reference became available.", playerId);
                    completeTransition(playerId, transitionToken);
                    return;
                }
                HytaleServer.SCHEDULED_EXECUTOR.schedule(
                        () -> waitForInteriorReady(playerId, targetWorld, entryPosition, updatedState, context, layout, firstInteriorTourPending, retriesRemaining - 1, transitionToken),
                        INTERIOR_READY_RETRY_MILLIS,
                        TimeUnit.MILLISECONDS
                );
                return;
            }
            Store<EntityStore> store = playerRef.getStore();
            Player livePlayer = store.getComponent(playerRef, Player.getComponentType());
            if (livePlayer == null) {
                if (retriesRemaining <= 0) {
                    LOGGER.at(Level.WARNING).log("Interior entry timed out for %s before the live player reference became available.", playerId);
                    completeTransition(playerId, transitionToken);
                    return;
                }
                HytaleServer.SCHEDULED_EXECUTOR.schedule(
                        () -> waitForInteriorReady(playerId, targetWorld, entryPosition, updatedState, context, layout, firstInteriorTourPending, retriesRemaining - 1, transitionToken),
                        INTERIOR_READY_RETRY_MILLIS,
                        TimeUnit.MILLISECONDS
                );
                return;
            }
            if (isInteriorReady(livePlayer, targetWorld, entryPosition)) {
                scheduleInteriorAnchors(targetWorld, layout, playerId, updatedState, firstInteriorTourPending);
                buildingVisualHandler.refreshBuildings(playerId, updatedState);
                livePlayer.sendMessage(Message.raw("Interior ready. Building placement and interaction are now available.").color("green"));
                completeTransition(playerId, transitionToken);
                uiNavigator.open(UiPageType.INTERIOR_MAIN, livePlayer, context, updatedState);
                return;
            }
            if (retriesRemaining <= 0) {
                LOGGER.at(Level.WARNING).log("Interior entry timed out for %s in world %s.", playerId, targetWorld.getName());
                livePlayer.sendMessage(Message.raw("Interior transfer timed out. Try /kd interior again.").color("red"));
                completeTransition(playerId, transitionToken);
                return;
            }
            HytaleServer.SCHEDULED_EXECUTOR.schedule(
                    () -> waitForInteriorReady(playerId, targetWorld, entryPosition, updatedState, context, layout, firstInteriorTourPending, retriesRemaining - 1, transitionToken),
                    INTERIOR_READY_RETRY_MILLIS,
                    TimeUnit.MILLISECONDS
            );
        });
    }

    private void scheduleInteriorAnchors(
            com.hypixel.hytale.server.core.universe.world.World world,
            InteriorLayout layout,
            UUID playerId,
            PlayerGameState updated,
            boolean firstInteriorTourPending
    ) {
        if (world == null || layout == null || playerId == null || updated == null) {
            return;
        }
        HytaleServer.SCHEDULED_EXECUTOR.schedule(
                () -> WorldTasks.executeSafe(world, "InteriorWorldHandler.scheduleInteriorAnchors", () -> {
                    if (!isInteriorSessionActive(playerId, world.getName())) {
                        return;
                    }
                    safeEnsureTourMarkers(world, layout, playerId, firstInteriorTourPending);
                    safeEnsurePopulationDisplays(world, layout, playerId, updated);
                }),
                400L,
                TimeUnit.MILLISECONDS
        );
    }

    private boolean isInteriorReady(Player livePlayer, com.hypixel.hytale.server.core.universe.world.World targetWorld, Vector3d entryPosition) {
        return livePlayer.getWorld() != null && targetWorld.getName().equals(livePlayer.getWorld().getName());
    }

    private Vector3d interiorEntryPosition(Player player, InteriorLayout layout) {
        Vector3f rotation = player.getTransformComponent().getRotation();
        Vector3d standingEntryPoint = new Vector3d(
                layout.entryPoint().getX(),
                layout.entryPoint().getY() + 1.0D,
                layout.entryPoint().getZ()
        );
        Transform targetTransform = new Transform(standingEntryPoint, rotation);
        return playerTeleportHandler.standingPosition(player, targetTransform.getPosition());
    }

    private Ref<EntityStore> resolveLivePlayerRef(UUID playerId) {
        var universe = com.hypixel.hytale.server.core.universe.Universe.get();
        if (universe == null) {
            return null;
        }
        for (var playerRef : universe.getPlayers()) {
            if (!playerRef.getUuid().equals(playerId)) {
                continue;
            }
            Ref<EntityStore> ref = playerRef.getReference();
            if (ref == null || !ref.isValid()) {
                return null;
            }
            return ref;
        }
        return null;
    }

    private void sendPlayerMessage(UUID playerId, Message message) {
        withLivePlayer(playerId, livePlayer -> livePlayer.sendMessage(message));
    }

    private void withLivePlayer(UUID playerId, Consumer<Player> action) {
        if (playerId == null || action == null) {
            return;
        }
        Ref<EntityStore> ref = resolveLivePlayerRef(playerId);
        if (ref == null || !ref.isValid()) {
            return;
        }
        Store<EntityStore> store = ref.getStore();
        com.hypixel.hytale.server.core.universe.world.World world = ((EntityStore) store.getExternalData()).getWorld();
        if (world == null) {
            return;
        }
        WorldTasks.executeSafe(world, "InteriorWorldHandler.withLivePlayer", () -> {
            if (!ref.isValid()) {
                return;
            }
            Player livePlayer = store.getComponent(ref, Player.getComponentType());
            if (livePlayer != null) {
                action.accept(livePlayer);
            }
        });
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    private String safeMessage(Throwable throwable) {
        if (throwable == null || throwable.getMessage() == null || throwable.getMessage().isBlank()) {
            return "unknown error";
        }
        return throwable.getMessage();
    }

    private void closeCurrentPage(Player player) {
        if (player == null || player.getPlayerRef() == null) {
            return;
        }
        var ref = player.getPlayerRef().getReference();
        if (ref == null || !ref.isValid()) {
            return;
        }
        var store = ref.getStore();
        if (!ref.isValid()) {
            return;
        }
        Player livePlayer = store.getComponent(ref, Player.getComponentType());
        if (livePlayer == null) {
            return;
        }
        livePlayer.getPageManager().setPage(ref, store, Page.None);
    }

    private boolean isInteriorSessionActive(UUID playerId, String worldName) {
        if (playerId == null || worldName == null || worldName.isBlank()) {
            return false;
        }
        PlayerSession session = sessionStore.get(playerId);
        if (session == null || session.gameState() == null || session.gameState().interiorSession() == null) {
            return false;
        }
        if (!worldName.equals(session.gameState().interiorSession().interiorWorldName())) {
            return false;
        }
        Ref<EntityStore> ref = resolveLivePlayerRef(playerId);
        if (ref == null || !ref.isValid()) {
            return false;
        }
        World currentWorld = ((EntityStore) ref.getStore().getExternalData()).getWorld();
        return currentWorld != null && worldName.equals(currentWorld.getName());
    }

    private CompletableFuture<Void> preloadInteriorChunks(World world, InteriorLayout layout) {
        if (world == null || layout == null) {
            return CompletableFuture.completedFuture(null);
        }
        Set<Long> chunkIndexes = new LinkedHashSet<>();
        registerChunk(chunkIndexes, layout.origin());
        registerChunk(chunkIndexes, layout.entryPoint());
        registerChunk(chunkIndexes, layout.exitPoint());
        registerChunk(chunkIndexes, layout.workerPlatformAnchor());
        registerChunk(chunkIndexes, layout.workerPortalAnchor());
        CompletableFuture<?>[] futures = chunkIndexes.stream()
                .map(world::getChunkAsync)
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures);
    }

    private void registerChunk(Set<Long> chunkIndexes, Vector3d position) {
        if (chunkIndexes == null || position == null) {
            return;
        }
        chunkIndexes.add(ChunkUtil.indexChunkFromBlock(
                (int) Math.floor(position.getX()),
                (int) Math.floor(position.getZ())
        ));
    }

    private boolean beginTransition(UUID playerId, TransitionKind kind) {
        if (playerId == null || kind == null) {
            return false;
        }
        while (true) {
            TransitionToken existing = transitionStates.get(playerId);
            if (existing == null) {
                TransitionToken token = new TransitionToken(transitionSequence.incrementAndGet(), kind, Instant.now());
                return transitionStates.putIfAbsent(playerId, token) == null;
            }
            if (!isTransitionExpired(existing)) {
                return false;
            }
            if (transitionStates.remove(playerId, existing)) {
                LOGGER.at(Level.WARNING).log(
                        "Cleared stale interior %s transition for %s after %s ms.",
                        existing.kind(),
                        playerId,
                        Duration.between(existing.startedAt(), Instant.now()).toMillis()
                );
            }
        }
    }

    private boolean isActiveTransition(UUID playerId, TransitionToken token) {
        return playerId != null && token != null && token.equals(transitionStates.get(playerId));
    }

    private void completeTransition(UUID playerId, TransitionToken token) {
        if (playerId == null || token == null) {
            return;
        }
        transitionStates.remove(playerId, token);
    }

    private boolean isTransitionExpired(TransitionToken token) {
        if (token == null || token.startedAt() == null) {
            return false;
        }
        return Duration.between(token.startedAt(), Instant.now()).compareTo(TRANSITION_TIMEOUT) > 0;
    }

    private enum TransitionKind {
        ENTER,
        EXIT
    }

    private record TransitionToken(long sequence, TransitionKind kind, Instant startedAt) {
    }
}

