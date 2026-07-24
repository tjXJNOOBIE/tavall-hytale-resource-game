package org.tavall.control.resource;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.control.player.IPlayerTeleportHandler;
import org.tavall.control.resource.IResourceNodePromptLaneHandler;
import org.tavall.control.domain.ResourceNodeData;
import org.tavall.control.tasks.WorldTasks;
import org.tavall.control.world.ResourceNodePromptLaneLayout;
import org.tavall.control.world.ResourceNodePromptLaneLayoutHandler;
import org.tavall.control.world.ResourceNodePromptLaneStructureHandler;

import java.util.Objects;

/**
 * Prepares the in-world node prompt lane and orients players for node interaction.
 */
public final class ResourceNodePromptLaneHandler implements IResourceNodePromptLaneHandler, IDependencyInjectableConcrete {
    private final ResourceNodePromptLaneLayoutHandler layoutHandler;
    private final ResourceNodePromptLaneStructureHandler structureHandler;
    private final IPlayerTeleportHandler playerTeleportHandler;

    public ResourceNodePromptLaneHandler(
            ResourceNodePromptLaneLayoutHandler layoutHandler,
            ResourceNodePromptLaneStructureHandler structureHandler,
            IPlayerTeleportHandler playerTeleportHandler
    ) {
        this.layoutHandler = Objects.requireNonNull(layoutHandler, "layoutHandler");
        this.structureHandler = Objects.requireNonNull(structureHandler, "structureHandler");
        this.playerTeleportHandler = Objects.requireNonNull(playerTeleportHandler, "playerTeleportHandler");
    }

    @Override
    public void alignPlayer(Player player, ResourceNodeData node) {
        ResourceNodePromptLaneLayout layout = layoutHandler.createLayout(node);
        Vector3d lookTarget = node.location().toVector();
        if (player == null || player.getWorld() == null) {
            return;
        }
        WorldTasks.executeSafe(player.getWorld(), "ResourceNodePromptLaneHandler.alignPlayer", () -> {
            structureHandler.ensurePromptLane(player.getWorld(), layout);
            playerTeleportHandler.orientPlayer(player, lookTarget);
        });
    }
}

