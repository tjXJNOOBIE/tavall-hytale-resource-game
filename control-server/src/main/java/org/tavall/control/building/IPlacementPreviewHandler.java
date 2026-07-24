package org.tavall.control.building;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import org.tavall.dependency.IDependencyInjectableInterface;
import org.tavall.control.domain.PlacementRequest;

import java.util.UUID;

/**
 * Renders temporary world-space placement previews.
 */
public interface IPlacementPreviewHandler extends IDependencyInjectableInterface {
    void showPreview(Player player, PlacementRequest request, Vector3i targetBlock);

    void clearPreview(UUID playerId);
}

