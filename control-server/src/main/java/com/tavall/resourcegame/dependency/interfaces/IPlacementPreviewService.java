package com.tavall.resourcegame.dependency.interfaces;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import com.tavall.resourcegame.domain.PlacementRequest;

import java.util.UUID;

/**
 * Renders temporary world-space placement previews.
 */
public interface IPlacementPreviewService extends IDependencyInjectableInterface {
    void showPreview(Player player, PlacementRequest request, Vector3i targetBlock);

    void clearPreview(UUID playerId);
}
