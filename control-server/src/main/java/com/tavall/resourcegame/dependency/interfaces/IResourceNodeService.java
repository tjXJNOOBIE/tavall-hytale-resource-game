package org.tavall.control.dependency.interfaces;

import com.hypixel.hytale.math.vector.Vector3d;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.tavall.control.domain.CastleEconomySnapshot;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.ResourceNodeData;
import org.tavall.control.domain.ResourceNodePillageResult;
import org.tavall.control.domain.ResourceNodeSummary;
import org.tavall.control.resources.ResourceType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IResourceNodeService extends IDependencyInjectableInterface {
    List<ResourceNodeData> listNodes(PlayerGameState state);

    Optional<ResourceNodeData> resolveNode(PlayerGameState state, String token);

    Optional<ResourceNodeData> findNode(PlayerGameState state, UUID nodeId);

    ResourceNodeSummary summary(PlayerGameState state, ResourceNodeData node);

    String summaryLine(PlayerGameState state, ResourceNodeData node, int index);

    int assignedTroops(PlayerGameState state);

    int availableTroops(PlayerGameState state);

    PlayerGameState placeNode(UUID playerId, ResourceType resourceType, String worldName, Vector3d position, Instant now);

    PlayerGameState removeNode(UUID playerId, UUID nodeId, Instant now);

    PlayerGameState clearNodes(UUID playerId, Instant now);

    PlayerGameState assignTroops(UUID playerId, UUID nodeId, int assignedTroops, Instant now);

    PlayerGameState addTroops(UUID playerId, UUID nodeId, int troopDelta, Instant now);

    PlayerGameState setStock(UUID playerId, UUID nodeId, int currentStock, Instant now);

    ResourceNodePillageResult pillageNode(UUID playerId, UUID nodeId, Instant now);

    PlayerGameState normalizeAssignments(PlayerGameState state, Instant now);

    PlayerGameState applyTick(PlayerGameState state, Instant now);

    PlayerGameState applyTick(PlayerGameState state, CastleEconomySnapshot economySnapshot, Instant now);
}
