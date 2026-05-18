package org.tavall.control.node;

import org.tavall.control.guild.GuildId;

import java.util.List;
import java.util.Optional;

public interface ResourceNodeRepository {
    ResourceNode saveResourceNode(ResourceNode resourceNode);

    Optional<ResourceNode> findResourceNode(ResourceNodeId nodeId);

    List<ResourceNode> findResourceNodesForGuild(GuildId guildId);
}
