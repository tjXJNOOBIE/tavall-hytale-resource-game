package org.tavall.control.npc;
import org.tavall.control.npc.NpcVisualSpawner;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.Frozen;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.protocol.InteractionType;
import org.tavall.control.interactions.OpenFarmsteadInteraction;
import com.hypixel.hytale.server.npc.NPCPlugin;
import org.tavall.dependency.IDependencyInjectableConcrete;
import it.unimi.dsi.fastutil.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Shared NPC marker spawner for readable in-world prototype visuals.
 */
public final class NpcVisualSpawner implements IDependencyInjectableConcrete {
    private static final Logger LOGGER = Logger.getLogger(NpcVisualSpawner.class.getName());

    public Ref<EntityStore> spawnNamed(Store<EntityStore> store, int roleIndex, Vector3d position, String label, float scale) {
        Pair<Ref<EntityStore>, ?> pair = NPCPlugin.get().spawnEntity(store, roleIndex, position, Vector3f.ZERO, null, null);
        Ref<EntityStore> ref = pair.first();
        if (ref != null && ref.isValid()) {
            store.putComponent(ref, DisplayNameComponent.getComponentType(), new DisplayNameComponent(Message.raw(label)));
            store.putComponent(ref, Nameplate.getComponentType(), new Nameplate(label));
            store.ensureComponent(ref, Frozen.getComponentType());
            attachResourceGameInteraction(store, ref);
            applyScale(store, ref, scale);
        }
        return ref;
    }

    public List<Ref<EntityStore>> spawnGroup(Store<EntityStore> store, int roleIndex, List<Vector3d> positions, int visibleCount, float scale) {
        List<Ref<EntityStore>> refs = new ArrayList<>();
        for (int index = 0; index < visibleCount && index < positions.size(); index++) {
            Pair<Ref<EntityStore>, ?> pair = NPCPlugin.get().spawnEntity(store, roleIndex, positions.get(index), Vector3f.ZERO, null, null);
            Ref<EntityStore> ref = pair.first();
            if (ref != null && ref.isValid()) {
                store.ensureComponent(ref, Frozen.getComponentType());
                attachResourceGameInteraction(store, ref);
                applyScale(store, ref, scale);
                refs.add(ref);
            }
        }
        return List.copyOf(refs);
    }

    public void applyScale(Store<EntityStore> store, Ref<EntityStore> ref, float scale) {
        if (ref == null || !ref.isValid()) {
            return;
        }
        float safeScale = Math.max(0.35F, scale);
        store.putComponent(ref, EntityScaleComponent.getComponentType(), new EntityScaleComponent(safeScale));
    }

    private void attachResourceGameInteraction(Store<EntityStore> store, Ref<EntityStore> ref) {
        Interactions interactions = new Interactions(Map.of(InteractionType.Secondary, OpenFarmsteadInteraction.ROOT_INTERACTION_ID));
        interactions.setInteractionHint("Right-click");
        store.putComponent(ref, Interactions.getComponentType(), interactions);
        LOGGER.info(() -> "Assigned Secondary interaction " + OpenFarmsteadInteraction.ROOT_INTERACTION_ID + " to NPC " + ref + ".");
    }
}
