package org.tavall.control.castle;
import org.tavall.control.protection.StructureProtectionHandler;
import org.tavall.control.world.WorldLabelHandler;
import org.tavall.control.player.PlayerSessionStore;
import org.tavall.control.player.PlayerSession;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.tavall.control.config.CastleAssetConfig;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.castle.ICastleSiteVisualHandler;
import org.tavall.control.player.IPlayerSessionStore;
import org.tavall.control.domain.CastleLocationData;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.world.CastleSiteLayout;
import org.tavall.control.world.CastleSiteLayoutHandler;
import org.tavall.control.world.CastleSiteStructureHandler;
import org.tavall.control.world.CastleSiteVisualRefs;
import org.tavall.control.world.ProtectedStructureType;
import org.tavall.control.tasks.WorldTasks;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.List;

/**
 * Maintains the main-world castle block marker plus its overhead label.
 */
public final class CastleSiteVisualHandler implements ICastleSiteVisualHandler, IDependencyInjectableConcrete {
    private static final Logger LOGGER = Logger.getLogger(CastleSiteVisualHandler.class.getName());
    private static final int CASTLE_PLACEMENT_BLOCK_RADIUS = 15;

    private final CastleAssetConfig castleAssetConfig;
    private final CastleSiteLayoutHandler layoutHandler;
    private final CastleSiteStructureHandler structureHandler;
    private final WorldLabelHandler worldLabelHandler;
    private final StructureProtectionHandler protectionHandler;
    private final IPlayerSessionStore sessionStore;
    private final Map<UUID, CastleSiteVisualRefs> siteRefs = new ConcurrentHashMap<>();

    public CastleSiteVisualHandler(
            CastleAssetConfig castleAssetConfig,
            CastleSiteLayoutHandler layoutHandler,
            CastleSiteStructureHandler structureHandler,
            WorldLabelHandler worldLabelHandler,
            StructureProtectionHandler protectionHandler,
            IPlayerSessionStore sessionStore
    ) {
        this.castleAssetConfig = castleAssetConfig;
        this.layoutHandler = layoutHandler;
        this.structureHandler = structureHandler;
        this.worldLabelHandler = worldLabelHandler;
        this.protectionHandler = protectionHandler;
        this.sessionStore = sessionStore;
    }

    @Override
    public void ensureSite(UUID playerId, PlayerGameState state) {
        refreshSite(playerId, state);
    }

    @Override
    public void refreshSite(UUID playerId, PlayerGameState state) {
        if (playerId == null || state == null || state.castleLocation() == null) {
            return;
        }
        CastleSiteVisualRefs refs = siteRefs.get(playerId);
        if (refs == null) {
            rebuildSite(playerId, state);
            return;
        }
        CastleLocationData location = state.castleLocation();
        if (!refs.worldName().equals(location.worldName())
                || floor(refs.worldPosition().getX()) != floor(location.x())
                || floor(refs.worldPosition().getY()) != floor(location.y())
                || floor(refs.worldPosition().getZ()) != floor(location.z())) {
            rebuildSite(playerId, state);
            return;
        }
        World world = Universe.get().getWorld(refs.worldName());
        if (world == null) {
            return;
        }
        WorldTasks.executeSafe(world, "CastleSiteVisualHandler.refreshLabels", () -> refreshLabels(world, playerId, state, refs));
    }

    @Override
    public void clearSite(UUID playerId) {
        CastleSiteVisualRefs refs = siteRefs.remove(playerId);
        protectionHandler.clearStructure(structureKey(playerId));
        if (refs == null) {
            return;
        }
        World world = Universe.get().getWorld(refs.worldName());
        if (world != null) {
            WorldTasks.executeSafe(world, "CastleSiteVisualHandler.clearRefsOnWorld", () -> clearRefsOnWorld(world, refs));
            return;
        }
        removeRefs(refs);
    }

    private void rebuildSite(UUID playerId, PlayerGameState state) {
        if (playerId == null || state == null || state.castleLocation() == null) {
            return;
        }
        World world = Universe.get().getWorld(state.castleLocation().worldName());
        if (world == null) {
            return;
        }
        CastleSiteVisualRefs previousRefs = siteRefs.remove(playerId);
        protectionHandler.clearStructure(structureKey(playerId));
        WorldTasks.executeSafe(world, "CastleSiteVisualHandler.rebuildSite", () -> {
            clearExistingSite(world, previousRefs);
            CastleSiteLayout layout = layoutHandler.createLayout(state.castleLocation());
            protectionHandler.replaceStructure(
                    structureKey(playerId),
                    playerId,
                    ProtectedStructureType.CASTLE,
                    world.getName(),
                    state.castleAssetType(),
                    structureHandler.ensureSite(world, layout)
            );
            protectionHandler.replacePlacementZone(
                    structureKey(playerId),
                    ProtectedStructureType.CASTLE,
                    world.getName(),
                    new Vector3i((int) Math.floor(state.castleLocation().x()), (int) Math.floor(state.castleLocation().y()), (int) Math.floor(state.castleLocation().z())),
                    CASTLE_PLACEMENT_BLOCK_RADIUS
            );
            List<Ref<EntityStore>> labelRefs = worldLabelHandler.spawnLabelStack(
                    world,
                    new Vector3d(state.castleLocation().x(), state.castleLocation().y() + 3.8D, state.castleLocation().z()),
                    castleLabelLines(playerId, state)
            );
            siteRefs.put(playerId, new CastleSiteVisualRefs(
                    world.getName(),
                    layout.origin(),
                    labelRefs,
                    castleLabelLines(playerId, state)
            ));
            LOGGER.info(() -> String.format(
                    "Castle site visuals refreshed for %s in world %s. citizens=%s troops=%s food=%s wood=%s iron=%s",
                    playerId,
                    world.getName(),
                    state.populationSummary().citizenCount(),
                    state.populationSummary().troopCount(),
                    state.resources().food(),
                    state.resources().wood(),
                    state.resources().iron()
            ));
        });
    }

    private void refreshLabels(World world, UUID playerId, PlayerGameState state, CastleSiteVisualRefs refs) {
        if (world == null || playerId == null || state == null || refs == null) {
            return;
        }
        List<String> lines = castleLabelLines(playerId, state);
        List<Ref<EntityStore>> labelRefs = refs.castleLabelRefs();
        boolean reusable = labelRefs.size() == lines.size()
                && labelRefs.stream().allMatch(ref -> ref != null && ref.isValid())
                && lines.equals(refs.castleLabelLines());
        if (!reusable) {
            safeRemoveLabels(labelRefs);
            List<Ref<EntityStore>> rebuilt = worldLabelHandler.spawnLabelStack(
                    world,
                    new Vector3d(state.castleLocation().x(), state.castleLocation().y() + 3.8D, state.castleLocation().z()),
                    lines
            );
            siteRefs.put(playerId, new CastleSiteVisualRefs(world.getName(), refs.worldPosition(), rebuilt, lines));
        }
    }

    private void safeRemoveLabels(List<Ref<EntityStore>> labelRefs) {
        if (labelRefs == null || labelRefs.isEmpty()) {
            return;
        }
        for (Ref<EntityStore> ref : labelRefs) {
            if (ref == null || !ref.isValid()) {
                continue;
            }
            try {
                ref.getStore().removeEntity(ref, RemoveReason.REMOVE);
            } catch (Throwable ignored) {
            }
        }
    }

    private void clearExistingSite(World targetWorld, CastleSiteVisualRefs refs) {
        if (refs == null) {
            return;
        }
        World previousWorld = Universe.get().getWorld(refs.worldName());
        if (previousWorld == null) {
            removeRefs(refs);
            return;
        }
        if (targetWorld != null && targetWorld.getName().equals(previousWorld.getName())) {
            clearRefsOnWorld(previousWorld, refs);
            return;
        }
        WorldTasks.executeSafe(previousWorld, "CastleSiteVisualHandler.clearRefsOnWorld", () -> clearRefsOnWorld(previousWorld, refs));
    }

    private void clearRefsOnWorld(World world, CastleSiteVisualRefs refs) {
        CastleLocationData location = new CastleLocationData(
                refs.worldName(),
                refs.worldPosition().getX(),
                refs.worldPosition().getY(),
                refs.worldPosition().getZ()
        );
        CastleSiteLayout layout = layoutHandler.createLayout(location);
        structureHandler.clearSite(world, layout);
        removeRefs(refs);
    }

    private void removeRefs(CastleSiteVisualRefs refs) {
        for (Ref<EntityStore> ref : refs.allRefs()) {
            if (ref != null && ref.isValid()) {
                ref.getStore().removeEntity(ref, RemoveReason.REMOVE);
            }
        }
    }

    private List<String> castleLabelLines(UUID playerId, PlayerGameState state) {
        return List.of(
                castleAssetConfig.displayName() + " | " + ownerName(playerId),
                "Troops " + state.populationSummary().troopCount() + " | Might " + state.populationSummary().might(),
                "Look within 7.5m | Right-click for stats, attack, friend"
        );
    }

    private String ownerName(UUID playerId) {
        PlayerSession session = sessionStore.get(playerId);
        if (session != null && session.profile() != null && session.profile().name() != null && !session.profile().name().isBlank()) {
            return session.profile().name();
        }
        return playerId.toString().substring(0, 8);
    }

    private String structureKey(UUID playerId) {
        return "castle:" + playerId;
    }

    private int floor(double value) {
        return (int) Math.floor(value);
    }
}

