package com.tavall.resourcegame.middleware.node;

import com.tavall.resourcegame.middleware.guild.GuildId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryResourceNodeRepository implements ResourceNodeRepository {
    private final Map<ResourceNodeId, ResourceNode> nodesById = new ConcurrentHashMap<>();

    @Override
    public ResourceNode saveResourceNode(ResourceNode resourceNode) {
        nodesById.put(resourceNode.nodeId(), resourceNode);
        return resourceNode;
    }

    @Override
    public Optional<ResourceNode> findResourceNode(ResourceNodeId nodeId) {
        return Optional.ofNullable(nodesById.get(nodeId));
    }

    @Override
    public List<ResourceNode> findResourceNodesForGuild(GuildId guildId) {
        List<ResourceNode> nodes = new ArrayList<>();
        for (ResourceNode node : nodesById.values()) {
            if (node.ownerGuildId().filter(guildId::equals).isPresent()) {
                nodes.add(node);
            }
        }
        return List.copyOf(nodes);
    }
}
