package org.tavall.api.minecraft.guild;

import org.tavall.dependency.IDependencyInjectableInterface;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface IGuildMetaDataBuilder extends IDependencyInjectableInterface {
    IGuildMetaDataBuilder guildId(GuildId guildId);

    IGuildMetaDataBuilder name(String name);

    IGuildMetaDataBuilder tag(String tag);

    IGuildMetaDataBuilder motto(String motto);

    IGuildMetaDataBuilder ownerPlayerId(UUID ownerPlayerId);

    IGuildMetaDataBuilder state(GuildState state);

    IGuildMetaDataBuilder guildLevel(int guildLevel);

    IGuildMetaDataBuilder createdAt(Instant createdAt);

    IGuildMetaDataBuilder updatedAt(Instant updatedAt);

    IGuildMetaDataBuilder settings(GuildSettings settings);

    IGuildMetaDataBuilder bank(GuildBankData bank);

    IGuildMetaDataBuilder buildings(Set<GuildBuildingType> buildings);

    IGuildMetaDataBuilder members(Map<UUID, PlayerGuildData> members);

    IGuildMetaDataBuilder rankPermissions(Map<GuildRank, Set<GuildPermission>> rankPermissions);

    IGuildMetaDataBuilder explicitPermissionExpansionAllowed(boolean explicitPermissionExpansionAllowed);

    IGuildMetaDataBuilder metadata(Map<String, String> metadata);

    IGuildMetaDataBuilder copyOf(GuildMetaData guildMetaData);

    GuildMetaData build();
}
