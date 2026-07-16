package org.tavall.api.minecraft.guild;

import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.dependency.annotations.DelegatesToInterface;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@DelegatesToInterface(getLinkedInterface = IGuildMetaDataBuilder.class)
public final class GuildMetaDataBuilder implements IGuildMetaDataBuilder, IDependencyInjectableConcrete {
    private final GuildId guildId;
    private final String name;
    private final String tag;
    private final String motto;
    private final UUID ownerPlayerId;
    private final GuildState state;
    private final int guildLevel;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final GuildSettings settings;
    private final GuildBankData bank;
    private final Set<GuildBuildingType> buildings;
    private final Map<UUID, PlayerGuildData> members;
    private final Map<GuildRank, Set<GuildPermission>> rankPermissions;
    private final boolean explicitPermissionExpansionAllowed;
    private final Map<String, String> metadata;

    public GuildMetaDataBuilder() {
        this(null, null, null, "", null, GuildState.ACTIVE, 1, Instant.now(), Instant.now(), null, null, Set.of(), Map.of(), Map.of(), true, Map.of());
    }

    private GuildMetaDataBuilder(
            GuildId guildId,
            String name,
            String tag,
            String motto,
            UUID ownerPlayerId,
            GuildState state,
            int guildLevel,
            Instant createdAt,
            Instant updatedAt,
            GuildSettings settings,
            GuildBankData bank,
            Set<GuildBuildingType> buildings,
            Map<UUID, PlayerGuildData> members,
            Map<GuildRank, Set<GuildPermission>> rankPermissions,
            boolean explicitPermissionExpansionAllowed,
            Map<String, String> metadata
    ) {
        this.guildId = guildId;
        this.name = name;
        this.tag = tag;
        this.motto = motto == null ? "" : motto;
        this.ownerPlayerId = ownerPlayerId;
        this.state = state == null ? GuildState.ACTIVE : state;
        this.guildLevel = guildLevel;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.updatedAt = updatedAt == null ? Instant.now() : updatedAt;
        this.settings = settings;
        this.bank = bank;
        this.buildings = buildings == null ? Set.of() : Set.copyOf(buildings);
        this.members = members == null ? Map.of() : Map.copyOf(members);
        this.rankPermissions = normalizeRankPermissions(rankPermissions);
        this.explicitPermissionExpansionAllowed = explicitPermissionExpansionAllowed;
        this.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    @Override
    public IGuildMetaDataBuilder guildId(GuildId guildId) {
        return new GuildMetaDataBuilder(guildId, name, tag, motto, ownerPlayerId, state, guildLevel, createdAt, updatedAt, settings, bank, buildings, members, rankPermissions, explicitPermissionExpansionAllowed, metadata);
    }

    @Override
    public IGuildMetaDataBuilder name(String name) {
        return new GuildMetaDataBuilder(guildId, name, tag, motto, ownerPlayerId, state, guildLevel, createdAt, updatedAt, settings, bank, buildings, members, rankPermissions, explicitPermissionExpansionAllowed, metadata);
    }

    @Override
    public IGuildMetaDataBuilder tag(String tag) {
        return new GuildMetaDataBuilder(guildId, name, tag, motto, ownerPlayerId, state, guildLevel, createdAt, updatedAt, settings, bank, buildings, members, rankPermissions, explicitPermissionExpansionAllowed, metadata);
    }

    @Override
    public IGuildMetaDataBuilder motto(String motto) {
        return new GuildMetaDataBuilder(guildId, name, tag, motto, ownerPlayerId, state, guildLevel, createdAt, updatedAt, settings, bank, buildings, members, rankPermissions, explicitPermissionExpansionAllowed, metadata);
    }

    @Override
    public IGuildMetaDataBuilder ownerPlayerId(UUID ownerPlayerId) {
        return new GuildMetaDataBuilder(guildId, name, tag, motto, ownerPlayerId, state, guildLevel, createdAt, updatedAt, settings, bank, buildings, members, rankPermissions, explicitPermissionExpansionAllowed, metadata);
    }

    @Override
    public IGuildMetaDataBuilder state(GuildState state) {
        return new GuildMetaDataBuilder(guildId, name, tag, motto, ownerPlayerId, state, guildLevel, createdAt, updatedAt, settings, bank, buildings, members, rankPermissions, explicitPermissionExpansionAllowed, metadata);
    }

    @Override
    public IGuildMetaDataBuilder guildLevel(int guildLevel) {
        return new GuildMetaDataBuilder(guildId, name, tag, motto, ownerPlayerId, state, guildLevel, createdAt, updatedAt, settings, bank, buildings, members, rankPermissions, explicitPermissionExpansionAllowed, metadata);
    }

    @Override
    public IGuildMetaDataBuilder createdAt(Instant createdAt) {
        return new GuildMetaDataBuilder(guildId, name, tag, motto, ownerPlayerId, state, guildLevel, createdAt, updatedAt, settings, bank, buildings, members, rankPermissions, explicitPermissionExpansionAllowed, metadata);
    }

    @Override
    public IGuildMetaDataBuilder updatedAt(Instant updatedAt) {
        return new GuildMetaDataBuilder(guildId, name, tag, motto, ownerPlayerId, state, guildLevel, createdAt, updatedAt, settings, bank, buildings, members, rankPermissions, explicitPermissionExpansionAllowed, metadata);
    }

    @Override
    public IGuildMetaDataBuilder settings(GuildSettings settings) {
        return new GuildMetaDataBuilder(guildId, name, tag, motto, ownerPlayerId, state, guildLevel, createdAt, updatedAt, settings, bank, buildings, members, rankPermissions, explicitPermissionExpansionAllowed, metadata);
    }

    @Override
    public IGuildMetaDataBuilder bank(GuildBankData bank) {
        return new GuildMetaDataBuilder(guildId, name, tag, motto, ownerPlayerId, state, guildLevel, createdAt, updatedAt, settings, bank, buildings, members, rankPermissions, explicitPermissionExpansionAllowed, metadata);
    }

    @Override
    public IGuildMetaDataBuilder buildings(Set<GuildBuildingType> buildings) {
        return new GuildMetaDataBuilder(guildId, name, tag, motto, ownerPlayerId, state, guildLevel, createdAt, updatedAt, settings, bank, buildings, members, rankPermissions, explicitPermissionExpansionAllowed, metadata);
    }

    @Override
    public IGuildMetaDataBuilder members(Map<UUID, PlayerGuildData> members) {
        return new GuildMetaDataBuilder(guildId, name, tag, motto, ownerPlayerId, state, guildLevel, createdAt, updatedAt, settings, bank, buildings, members, rankPermissions, explicitPermissionExpansionAllowed, metadata);
    }

    @Override
    public IGuildMetaDataBuilder rankPermissions(Map<GuildRank, Set<GuildPermission>> rankPermissions) {
        return new GuildMetaDataBuilder(guildId, name, tag, motto, ownerPlayerId, state, guildLevel, createdAt, updatedAt, settings, bank, buildings, members, rankPermissions, explicitPermissionExpansionAllowed, metadata);
    }

    @Override
    public IGuildMetaDataBuilder explicitPermissionExpansionAllowed(boolean explicitPermissionExpansionAllowed) {
        return new GuildMetaDataBuilder(guildId, name, tag, motto, ownerPlayerId, state, guildLevel, createdAt, updatedAt, settings, bank, buildings, members, rankPermissions, explicitPermissionExpansionAllowed, metadata);
    }

    @Override
    public IGuildMetaDataBuilder metadata(Map<String, String> metadata) {
        return new GuildMetaDataBuilder(guildId, name, tag, motto, ownerPlayerId, state, guildLevel, createdAt, updatedAt, settings, bank, buildings, members, rankPermissions, explicitPermissionExpansionAllowed, metadata);
    }

    @Override
    public IGuildMetaDataBuilder copyOf(GuildMetaData guildMetaData) {
        Objects.requireNonNull(guildMetaData, "guildMetaData");
        return new GuildMetaDataBuilder(
                guildMetaData.guildId(),
                guildMetaData.name(),
                guildMetaData.tag(),
                guildMetaData.motto(),
                guildMetaData.ownerPlayerId(),
                guildMetaData.state(),
                guildMetaData.guildLevel(),
                guildMetaData.createdAt(),
                guildMetaData.updatedAt(),
                guildMetaData.settings(),
                guildMetaData.bank(),
                guildMetaData.buildings(),
                guildMetaData.members(),
                guildMetaData.rankPermissions(),
                guildMetaData.explicitPermissionExpansionAllowed(),
                guildMetaData.metadata()
        );
    }

    @Override
    public GuildMetaData build() {
        GuildSettings resolvedSettings = settings == null ? GuildSettings.defaults() : settings;
        GuildBankData resolvedBank = bank;
        if (resolvedBank == null) {
            Objects.requireNonNull(guildId, "guildId");
            resolvedBank = new GuildBankData(guildId, 0L, Map.of(), createdAt, Map.of());
        }
        return new GuildMetaData(
                guildId,
                name,
                tag,
                motto,
                ownerPlayerId,
                state,
                guildLevel,
                createdAt,
                updatedAt,
                resolvedSettings,
                resolvedBank,
                buildings,
                members,
                rankPermissions,
                explicitPermissionExpansionAllowed,
                metadata
        );
    }

    private static Map<GuildRank, Set<GuildPermission>> normalizeRankPermissions(Map<GuildRank, Set<GuildPermission>> rankPermissions) {
        if (rankPermissions == null || rankPermissions.isEmpty()) {
            return Map.of();
        }
        Map<GuildRank, Set<GuildPermission>> normalized = new java.util.EnumMap<>(GuildRank.class);
        for (Map.Entry<GuildRank, Set<GuildPermission>> entry : rankPermissions.entrySet()) {
            normalized.put(entry.getKey(), entry.getValue() == null ? Set.of() : Set.copyOf(entry.getValue()));
        }
        return Map.copyOf(normalized);
    }
}
