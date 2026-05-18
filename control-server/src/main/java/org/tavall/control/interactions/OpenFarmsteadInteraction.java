package org.tavall.control.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.tavall.control.bootstrap.IResourceGameDomain;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class OpenFarmsteadInteraction extends SimpleInstantInteraction implements IResourceGameDomain {
    public static final String ROOT_INTERACTION_ID = "Tavall_Open_Farmstead_Menu";
    public static final String LEGACY_ROOT_INTERACTION_ID = "OpenFarmstead";
    public static final String INTERACTION_TYPE_ID = "tavall:open_farmstead_menu";
    public static final String LEGACY_INTERACTION_TYPE_ID = "OpenFarmstead";
    public static final BuilderCodec<OpenFarmsteadInteraction> CODEC = BuilderCodec.builder(
            OpenFarmsteadInteraction.class,
            OpenFarmsteadInteraction::new,
            SimpleInstantInteraction.CODEC
    ).build();
    private static final double MAX_INTERACTION_DISTANCE = 6.0D;
    private static final Logger LOGGER = Logger.getLogger(OpenFarmsteadInteraction.class.getName());
    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);

    public OpenFarmsteadInteraction() {
    }

    public static void registerCodec() {
        if (!REGISTERED.compareAndSet(false, true)) {
            return;
        }
        try {
            Interaction.CODEC.register(INTERACTION_TYPE_ID, OpenFarmsteadInteraction.class, CODEC);
            Interaction.CODEC.register(LEGACY_INTERACTION_TYPE_ID, OpenFarmsteadInteraction.class, CODEC);
            LOGGER.info(() -> "Registered Resource Game Hytale interaction codecs: "
                    + INTERACTION_TYPE_ID + ", " + LEGACY_INTERACTION_TYPE_ID + ".");
        } catch (RuntimeException ex) {
            REGISTERED.set(false);
            LOGGER.log(Level.WARNING, "Failed to register Resource Game interaction codec: " + INTERACTION_TYPE_ID, ex);
            throw ex;
        }
    }

    @Override
    protected void firstRun(InteractionType interactionType, InteractionContext interactionContext, CooldownHandler cooldownHandler) {
        if (interactionType != InteractionType.Secondary || interactionContext == null) {
            return;
        }
        Ref<EntityStore> playerRef = interactionContext.getOwningEntity();
        Ref<EntityStore> targetRef = resolveTargetRef(interactionContext, playerRef);
        LOGGER.info(() -> "Server received Secondary Farmstead interaction. playerRef=" + playerRef + " targetRef=" + targetRef + ".");
        Player player = resolvePlayer(playerRef, interactionContext);
        if (player == null) {
            LOGGER.warning("OpenFarmsteadInteraction ran without a player entity.");
            return;
        }
        if (targetRef == null || !targetRef.isValid()) {
            player.sendMessage(Message.raw("No kingdom target was selected.").color("yellow"));
            LOGGER.warning(() -> "Farmstead interaction rejected for " + player.getDisplayName() + " because no valid target was supplied.");
            return;
        }
        if (!isTargetWithinRange(player, targetRef, interactionContext)) {
            player.sendMessage(Message.raw("Move closer to interact with that kingdom target.").color("yellow"));
            LOGGER.warning(() -> "Farmstead interaction rejected for " + player.getDisplayName()
                    + " because target was outside " + MAX_INTERACTION_DISTANCE + " blocks. target=" + targetRef + ".");
            return;
        }
        if (openWithCustomEntityHandler(player, targetRef)) {
            return;
        }
        if (openWithWorkerNpcHandler(player, targetRef)) {
            return;
        }
        if (openWithBuildingHandler(player, targetRef)) {
            return;
        }
        player.sendMessage(Message.raw("Kingdom target is not linked to a UI yet.").color("yellow"));
        LOGGER.info(() -> "OpenFarmsteadInteraction found no Resource Game UI handler for target " + targetRef + ".");
    }

    private Ref<EntityStore> resolveTargetRef(InteractionContext interactionContext, Ref<EntityStore> playerRef) {
        Ref<EntityStore> targetRef = interactionContext.getTargetEntity();
        if (targetRef != null) {
            return targetRef;
        }
        Ref<EntityStore> runningEntityRef = interactionContext.getEntity();
        if (runningEntityRef != null && !runningEntityRef.equals(playerRef)) {
            return runningEntityRef;
        }
        return null;
    }

    private Player resolvePlayer(Ref<EntityStore> playerRef, InteractionContext interactionContext) {
        if (playerRef == null || !playerRef.isValid()) {
            return null;
        }
        Entity entity = EntityUtils.getEntity(playerRef, interactionContext.getCommandBuffer());
        if (entity instanceof Player player) {
            return player;
        }
        return null;
    }

    private boolean isTargetWithinRange(Player player, Ref<EntityStore> targetRef, InteractionContext interactionContext) {
        Entity target = EntityUtils.getEntity(targetRef, interactionContext.getCommandBuffer());
        if (target == null) {
            LOGGER.warning(() -> "Farmstead interaction range validation failed because target entity could not be resolved: " + targetRef + ".");
            return false;
        }
        TransformComponent playerTransform = player.getTransformComponent();
        TransformComponent targetTransform = targetRef.getStore().getComponent(targetRef, TransformComponent.getComponentType());
        if (playerTransform == null || playerTransform.getPosition() == null || targetTransform == null || targetTransform.getPosition() == null) {
            LOGGER.warning(() -> "Farmstead interaction range validation failed because player or target position is missing. player="
                    + player.getDisplayName() + " target=" + targetRef + ".");
            return false;
        }
        return distanceSquared(playerTransform.getPosition(), targetTransform.getPosition()) <= MAX_INTERACTION_DISTANCE * MAX_INTERACTION_DISTANCE;
    }

    private double distanceSquared(Vector3d playerPosition, Vector3d targetPosition) {
        double dx = playerPosition.getX() - targetPosition.getX();
        double dy = playerPosition.getY() - targetPosition.getY();
        double dz = playerPosition.getZ() - targetPosition.getZ();
        return (dx * dx) + (dy * dy) + (dz * dz);
    }

    private boolean openWithCustomEntityHandler(Player player, Ref<EntityStore> targetRef) {
        try {
            return getCustomEntitySpawnHandler().openFromTarget(player, targetRef);
        } catch (IllegalStateException ex) {
            LOGGER.log(Level.FINE, "Custom entity Farmstead interaction route is not registered.", ex);
            return false;
        } catch (RuntimeException ex) {
            LOGGER.log(Level.WARNING, "Custom entity Farmstead interaction route failed for target " + targetRef + ".", ex);
            return false;
        }
    }

    private boolean openWithWorkerNpcHandler(Player player, Ref<EntityStore> targetRef) {
        try {
            return getWorkerNpcInteractionHandler().openFromTarget(player, targetRef);
        } catch (IllegalStateException ex) {
            LOGGER.log(Level.FINE, "Worker NPC Farmstead interaction route is not registered.", ex);
            return false;
        } catch (RuntimeException ex) {
            LOGGER.log(Level.WARNING, "Worker NPC Farmstead interaction route failed for target " + targetRef + ".", ex);
            return false;
        }
    }

    private boolean openWithBuildingHandler(Player player, Ref<EntityStore> targetRef) {
        try {
            return getBuildingInteractionHandler().openFromTarget(player, targetRef);
        } catch (IllegalStateException ex) {
            LOGGER.log(Level.FINE, "Building Farmstead interaction route is not registered.", ex);
            return false;
        } catch (RuntimeException ex) {
            LOGGER.log(Level.WARNING, "Building Farmstead interaction route failed for target " + targetRef + ".", ex);
            return false;
        }
    }
}

