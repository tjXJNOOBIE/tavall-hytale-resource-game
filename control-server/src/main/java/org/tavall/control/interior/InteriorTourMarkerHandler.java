package org.tavall.control.interior;
import org.tavall.control.interior.InteriorTourMarkerHandler;
import org.tavall.control.world.WorldLabelHandler;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.minecraft.domain.interior.InteriorLayout;
import org.tavall.minecraft.domain.interior.InteriorTourStop;
import org.tavall.control.tasks.WorldTasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Spawns and clears first-join interior tour labels without NPC placeholders.
 */
public final class InteriorTourMarkerHandler implements IDependencyInjectableConcrete {
    private static final Logger LOGGER = Logger.getLogger(InteriorTourMarkerHandler.class.getName());

    private final WorldLabelHandler worldLabelHandler;
    private final Map<UUID, List<Ref<EntityStore>>> markerRefs = new ConcurrentHashMap<>();

    public InteriorTourMarkerHandler(WorldLabelHandler worldLabelHandler) {
        this.worldLabelHandler = worldLabelHandler;
    }

    public void ensureTourMarkers(UUID playerId, World world, InteriorLayout layout, boolean tutorialPending) {
        if (!tutorialPending) {
            clearTourMarkers(playerId);
            return;
        }

        List<Ref<EntityStore>> existing = markerRefs.get(playerId);
        if (existing != null && existing.stream().allMatch(ref -> ref != null && ref.isValid())) {
            return;
        }

        clearTourMarkers(playerId);
        List<Ref<EntityStore>> refs = new ArrayList<>();
        for (InteriorTourStop stop : layout.tourStops()) {
            Ref<EntityStore> ref = worldLabelHandler.spawnLabel(world, stop.position().add(0.0D, 1.8D, 0.0D), stop.displayLabel());
            if (ref != null && ref.isValid()) {
                refs.add(ref);
            }
        }
        markerRefs.put(playerId, List.copyOf(refs));
        LOGGER.info(() -> String.format(
                "Interior tour labels ready for %s in world %s. stops=%s",
                playerId,
                world.getName(),
                refs.size()
        ));
    }

    public void clearTourMarkers(UUID playerId) {
        List<Ref<EntityStore>> refs = markerRefs.remove(playerId);
        if (refs == null) {
            return;
        }
        for (Ref<EntityStore> ref : refs) {
            removeSafely(ref);
        }
    }

    private void removeSafely(Ref<EntityStore> ref) {
        if (ref == null || !ref.isValid()) {
            return;
        }
        Store<EntityStore> store = ref.getStore();
        Runnable remove = () -> {
            if (ref.isValid()) {
                store.removeEntity(ref, RemoveReason.REMOVE);
            }
        };
        if (store.getExternalData() instanceof EntityStore entityStore) {
            World world = entityStore.getWorld();
            if (world != null) {
                WorldTasks.executeSafe(world, "InteriorTourMarkerHandler.removeSafely", remove);
                return;
            }
        }
        remove.run();
    }
}
