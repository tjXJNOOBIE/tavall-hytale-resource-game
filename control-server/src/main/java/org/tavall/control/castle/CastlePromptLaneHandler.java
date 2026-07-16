package org.tavall.control.castle;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.castle.ICastlePromptLaneHandler;
import org.tavall.control.player.IPlayerTeleportHandler;
import org.tavall.control.domain.CastleLocationData;
import org.tavall.control.tasks.WorldTasks;
import org.tavall.control.world.CastlePromptLaneLayout;
import org.tavall.control.world.CastlePromptLaneLayoutHandler;
import org.tavall.control.world.CastlePromptLaneStructureHandler;

import java.util.Objects;

/**
 * Prepares the in-world prompt lane and orients players for castle prompt interaction.
 */
public final class CastlePromptLaneHandler implements ICastlePromptLaneHandler, IDependencyInjectableConcrete {
    private final CastlePromptLaneLayoutHandler layoutHandler;
    private final CastlePromptLaneStructureHandler structureHandler;
    private final IPlayerTeleportHandler playerTeleportHandler;

    public CastlePromptLaneHandler(
            CastlePromptLaneLayoutHandler layoutHandler,
            CastlePromptLaneStructureHandler structureHandler,
            IPlayerTeleportHandler playerTeleportHandler
    ) {
        this.layoutHandler = Objects.requireNonNull(layoutHandler, "layoutHandler");
        this.structureHandler = Objects.requireNonNull(structureHandler, "structureHandler");
        this.playerTeleportHandler = Objects.requireNonNull(playerTeleportHandler, "playerTeleportHandler");
    }

    public void alignPlayer(Player player, CastleLocationData castleLocation) {
        CastlePromptLaneLayout layout = layoutHandler.createLayout(castleLocation);
        Vector3d lookTarget = castleLocation.toVector();
        if (player == null || player.getWorld() == null) {
            return;
        }
        WorldTasks.executeSafe(player.getWorld(), "CastlePromptLaneHandler.alignPlayer", () -> {
            structureHandler.ensurePromptLane(player.getWorld(), layout);
            playerTeleportHandler.orientPlayer(player, lookTarget);
        });
    }
}

