package org.tavall.control.resource;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.dependency.interfaces.IPlayerTeleportService;
import org.tavall.control.dependency.interfaces.IResourceNodePromptLaneService;
import org.tavall.control.domain.ResourceNodeData;
import org.tavall.control.tasks.WorldTasks;
import org.tavall.control.world.ResourceNodePromptLaneLayout;
import org.tavall.control.world.ResourceNodePromptLaneLayoutService;
import org.tavall.control.world.ResourceNodePromptLaneStructureService;

import java.util.Objects;

/**
 * Prepares the in-world node prompt lane and orients players for node interaction.
 */
public final class ResourceNodePromptLaneService implements IResourceNodePromptLaneService, IDependencyInjectableConcrete {
    private final ResourceNodePromptLaneLayoutService layoutService;
    private final ResourceNodePromptLaneStructureService structureService;
    private final IPlayerTeleportService playerTeleportService;

    public ResourceNodePromptLaneService(
            ResourceNodePromptLaneLayoutService layoutService,
            ResourceNodePromptLaneStructureService structureService,
            IPlayerTeleportService playerTeleportService
    ) {
        this.layoutService = Objects.requireNonNull(layoutService, "layoutService");
        this.structureService = Objects.requireNonNull(structureService, "structureService");
        this.playerTeleportService = Objects.requireNonNull(playerTeleportService, "playerTeleportService");
    }

    @Override
    public void alignPlayer(Player player, ResourceNodeData node) {
        ResourceNodePromptLaneLayout layout = layoutService.createLayout(node);
        Vector3d lookTarget = node.location().toVector();
        if (player == null || player.getWorld() == null) {
            return;
        }
        WorldTasks.executeSafe(player.getWorld(), "ResourceNodePromptLaneService.alignPlayer", () -> {
            structureService.ensurePromptLane(player.getWorld(), layout);
            playerTeleportService.orientPlayer(player, lookTarget);
        });
    }
}
