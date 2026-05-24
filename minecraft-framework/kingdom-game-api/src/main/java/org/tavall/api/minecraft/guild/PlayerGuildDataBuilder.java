package org.tavall.api.minecraft.guild;

import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.dependency.annotations.DelegatesToInterface;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@DelegatesToInterface(getLinkedInterface = IPlayerGuildDataBuilder.class)
public final class PlayerGuildDataBuilder implements IPlayerGuildDataBuilder, IDependencyInjectableConcrete {
    private final GuildId guildId;
    private final UUID universalPlayerId;
    private final GuildRank rank;
    private final Set<GuildPermission> explicitPermissions;
    private final Set<GuildRole> roles;
    private final Instant joinedAt;
    private final Map<String, String> metadata;

    public PlayerGuildDataBuilder() {
        this(null, null, GuildRank.TIER_I, Set.of(), Set.of(), Instant.now(), Map.of());
    }

    private PlayerGuildDataBuilder(
            GuildId guildId,
            UUID universalPlayerId,
            GuildRank rank,
            Set<GuildPermission> explicitPermissions,
            Set<GuildRole> roles,
            Instant joinedAt,
            Map<String, String> metadata
    ) {
        this.guildId = guildId;
        this.universalPlayerId = universalPlayerId;
        this.rank = rank == null ? GuildRank.TIER_I : rank;
        this.explicitPermissions = explicitPermissions == null ? Set.of() : Set.copyOf(explicitPermissions);
        this.roles = roles == null ? Set.of() : Set.copyOf(roles);
        this.joinedAt = joinedAt == null ? Instant.now() : joinedAt;
        this.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    @Override
    public IPlayerGuildDataBuilder guildId(GuildId guildId) {
        return new PlayerGuildDataBuilder(guildId, universalPlayerId, rank, explicitPermissions, roles, joinedAt, metadata);
    }

    @Override
    public IPlayerGuildDataBuilder universalPlayerId(UUID universalPlayerId) {
        return new PlayerGuildDataBuilder(guildId, universalPlayerId, rank, explicitPermissions, roles, joinedAt, metadata);
    }

    @Override
    public IPlayerGuildDataBuilder rank(GuildRank rank) {
        return new PlayerGuildDataBuilder(guildId, universalPlayerId, rank, explicitPermissions, roles, joinedAt, metadata);
    }

    @Override
    public IPlayerGuildDataBuilder explicitPermissions(Set<GuildPermission> explicitPermissions) {
        return new PlayerGuildDataBuilder(guildId, universalPlayerId, rank, explicitPermissions, roles, joinedAt, metadata);
    }

    @Override
    public IPlayerGuildDataBuilder roles(Set<GuildRole> roles) {
        return new PlayerGuildDataBuilder(guildId, universalPlayerId, rank, explicitPermissions, roles, joinedAt, metadata);
    }

    @Override
    public IPlayerGuildDataBuilder joinedAt(Instant joinedAt) {
        return new PlayerGuildDataBuilder(guildId, universalPlayerId, rank, explicitPermissions, roles, joinedAt, metadata);
    }

    @Override
    public IPlayerGuildDataBuilder metadata(Map<String, String> metadata) {
        return new PlayerGuildDataBuilder(guildId, universalPlayerId, rank, explicitPermissions, roles, joinedAt, metadata);
    }

    @Override
    public IPlayerGuildDataBuilder copyOf(PlayerGuildData playerGuildData) {
        Objects.requireNonNull(playerGuildData, "playerGuildData");
        return new PlayerGuildDataBuilder(
                playerGuildData.guildId(),
                playerGuildData.universalPlayerId(),
                playerGuildData.rank(),
                playerGuildData.explicitPermissions(),
                playerGuildData.roles(),
                playerGuildData.joinedAt(),
                playerGuildData.metadata()
        );
    }

    @Override
    public PlayerGuildData build() {
        return new PlayerGuildData(guildId, universalPlayerId, rank, explicitPermissions, roles, joinedAt, metadata);
    }
}
