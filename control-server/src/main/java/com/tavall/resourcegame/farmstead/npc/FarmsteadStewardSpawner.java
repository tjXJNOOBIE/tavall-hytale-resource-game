package org.tavall.control.farmstead.npc;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.services.NpcRoleResolver;
import org.tavall.control.services.NpcVisualSpawner;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class FarmsteadStewardSpawner implements IDependencyInjectableConcrete {
    public static final String NPC_ID = "tavall:farmstead_steward";
    public static final String DISPLAY_NAME = "Farmstead Steward";
    private static final String PREFERRED_ROLE_NAME = "Outlander_Peon";
    private static final float STEWARD_SCALE = 1.0F;
    private static final Logger LOGGER = Logger.getLogger(FarmsteadStewardSpawner.class.getName());

    private final NpcVisualSpawner npcVisualSpawner;
    private final NpcRoleResolver npcRoleResolver;

    public FarmsteadStewardSpawner(NpcVisualSpawner npcVisualSpawner, NpcRoleResolver npcRoleResolver) {
        this.npcVisualSpawner = Objects.requireNonNull(npcVisualSpawner, "npcVisualSpawner");
        this.npcRoleResolver = Objects.requireNonNull(npcRoleResolver, "npcRoleResolver");
    }

    public Ref<EntityStore> spawnFarmsteadStewardAt(Store<EntityStore> store, Vector3d position) {
        if (store == null || position == null) {
            LOGGER.warning("Farmstead Steward spawn skipped because the target store or position is missing.");
            return null;
        }
        int roleIndex = npcRoleResolver.resolveRoleIndex(PREFERRED_ROLE_NAME);
        if (roleIndex < 0) {
            LOGGER.warning("Farmstead Steward spawn failed because no usable NPC role index was resolved.");
            return null;
        }
        try {
            Ref<EntityStore> ref = npcVisualSpawner.spawnNamed(store, roleIndex, position, DISPLAY_NAME, STEWARD_SCALE);
            if (ref == null || !ref.isValid()) {
                LOGGER.warning("Farmstead Steward spawn failed because the NPC plugin returned no valid entity reference.");
                return null;
            }
            LOGGER.info(() -> "Spawned " + DISPLAY_NAME + " (" + NPC_ID + ") at "
                    + position.getX() + ", " + position.getY() + ", " + position.getZ() + ".");
            return ref;
        } catch (RuntimeException ex) {
            LOGGER.log(Level.SEVERE, "Farmstead Steward spawn failed at "
                    + position.getX() + ", " + position.getY() + ", " + position.getZ() + ".", ex);
            return null;
        }
    }
}
