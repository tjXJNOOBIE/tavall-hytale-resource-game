package org.tavall.control.world;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Tracks world refs for a single rendered building scene.
 */
public final class CastleBuildingVisualRefs {
    private final String worldName;
    private final Vector3d worldPosition;
    private final List<Ref<EntityStore>> labelRefs;
    private final List<Ref<EntityStore>> modelRefs;

    public CastleBuildingVisualRefs(
            String worldName,
            Vector3d worldPosition,
            List<Ref<EntityStore>> labelRefs,
            List<Ref<EntityStore>> modelRefs
    ) {
        this.worldName = Objects.requireNonNull(worldName, "worldName");
        this.worldPosition = Objects.requireNonNull(worldPosition, "worldPosition");
        this.labelRefs = labelRefs == null ? List.of() : List.copyOf(labelRefs);
        this.modelRefs = modelRefs == null ? List.of() : List.copyOf(modelRefs);
    }

    public String worldName() {
        return worldName;
    }

    public Vector3d worldPosition() {
        return worldPosition;
    }

    public boolean matches(Ref<EntityStore> targetRef) {
        if (targetRef == null) {
            return false;
        }
        return labelRefs.stream().anyMatch(ref -> Objects.equals(ref, targetRef))
                || modelRefs.stream().anyMatch(ref -> Objects.equals(ref, targetRef));
    }

    public List<Ref<EntityStore>> allRefs() {
        List<Ref<EntityStore>> refs = new ArrayList<>();
        refs.addAll(labelRefs);
        refs.addAll(modelRefs);
        return List.copyOf(refs);
    }
}
