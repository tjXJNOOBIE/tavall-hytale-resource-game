package org.tavall.api.minecraft.guild;

import org.tavall.dependency.IDependencyInjectableInterface;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface IPlayerGuildDataBuilder extends IDependencyInjectableInterface {
    IPlayerGuildDataBuilder guildId(GuildId guildId);

    IPlayerGuildDataBuilder universalPlayerId(UUID universalPlayerId);

    IPlayerGuildDataBuilder rank(GuildRank rank);

    IPlayerGuildDataBuilder explicitPermissions(Set<GuildPermission> explicitPermissions);

    IPlayerGuildDataBuilder roles(Set<GuildRole> roles);

    IPlayerGuildDataBuilder joinedAt(Instant joinedAt);

    IPlayerGuildDataBuilder metadata(Map<String, String> metadata);

    IPlayerGuildDataBuilder copyOf(PlayerGuildData playerGuildData);

    PlayerGuildData build();
}
