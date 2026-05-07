package com.tavall.hytale.resourcegame.middleware.node;

import com.tavall.hytale.resourcegame.middleware.guild.GuildId;

import java.util.List;
import java.util.Optional;

public interface ResourceNodeRepository {
    ResourceNode saveResourceNode(ResourceNode resourceNode);

    Optional<ResourceNode> findResourceNode(ResourceNodeId nodeId);

    List<ResourceNode> findResourceNodesForGuild(GuildId guildId);
}
