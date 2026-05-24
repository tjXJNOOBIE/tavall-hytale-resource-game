package org.tavall.api.minecraft.guild;

import org.tavall.dependency.DependencyLoaderAccess;
import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.dependency.annotations.DelegatesToInterface;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@DelegatesToInterface(getLinkedInterface = IGuildCreationBuilder.class)
public final class GuildCreationBuilder implements IGuildCreationBuilder, IDependencyInjectableConcrete {
    private final UUID creatorPlayerId;
    private final String guildName;
    private final String tag;
    private final String motto;
    private final boolean publicGuild;
    private final boolean inviteOnly;
    private final long startingCoins;
    private final Map<String, String> metadata;

    public GuildCreationBuilder() {
        this(null, null, null, "", true, false, 0L, Map.of());
    }

    private GuildCreationBuilder(
            UUID creatorPlayerId,
            String guildName,
            String tag,
            String motto,
            boolean publicGuild,
            boolean inviteOnly,
            long startingCoins,
            Map<String, String> metadata
    ) {
        this.creatorPlayerId = creatorPlayerId;
        this.guildName = guildName;
        this.tag = tag;
        this.motto = motto == null ? "" : motto;
        this.publicGuild = publicGuild;
        this.inviteOnly = inviteOnly;
        this.startingCoins = startingCoins;
        this.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    @Override
    public IGuildCreationBuilder creator(UUID creatorPlayerId) {
        return new GuildCreationBuilder(creatorPlayerId, guildName, tag, motto, publicGuild, inviteOnly, startingCoins, metadata);
    }

    @Override
    public IGuildCreationBuilder guildName(String guildName) {
        return new GuildCreationBuilder(creatorPlayerId, guildName, tag, motto, publicGuild, inviteOnly, startingCoins, metadata);
    }

    @Override
    public IGuildCreationBuilder tag(String tag) {
        return new GuildCreationBuilder(creatorPlayerId, guildName, tag, motto, publicGuild, inviteOnly, startingCoins, metadata);
    }

    @Override
    public IGuildCreationBuilder motto(String motto) {
        return new GuildCreationBuilder(creatorPlayerId, guildName, tag, motto, publicGuild, inviteOnly, startingCoins, metadata);
    }

    @Override
    public IGuildCreationBuilder publicGuild(boolean publicGuild) {
        return new GuildCreationBuilder(creatorPlayerId, guildName, tag, motto, publicGuild, inviteOnly, startingCoins, metadata);
    }

    @Override
    public IGuildCreationBuilder inviteOnly(boolean inviteOnly) {
        return new GuildCreationBuilder(creatorPlayerId, guildName, tag, motto, publicGuild, inviteOnly, startingCoins, metadata);
    }

    @Override
    public IGuildCreationBuilder startingCoins(long startingCoins) {
        return new GuildCreationBuilder(creatorPlayerId, guildName, tag, motto, publicGuild, inviteOnly, startingCoins, metadata);
    }

    @Override
    public IGuildCreationBuilder metadata(Map<String, String> metadata) {
        return new GuildCreationBuilder(creatorPlayerId, guildName, tag, motto, publicGuild, inviteOnly, startingCoins, metadata);
    }

    @Override
    public IGuildCreationBuilder copyOf(GuildMetaData guildMetaData) {
        Objects.requireNonNull(guildMetaData, "guildMetaData");
        Map<String, String> copiedMetadata = new HashMap<>(guildMetaData.metadata());
        copiedMetadata.putIfAbsent("creatorPlayerId", guildMetaData.ownerPlayerId().toString());
        return new GuildCreationBuilder(
                guildMetaData.ownerPlayerId(),
                guildMetaData.name(),
                guildMetaData.tag(),
                guildMetaData.motto(),
                guildMetaData.settings().publicGuild(),
                guildMetaData.settings().inviteOnly(),
                guildMetaData.bank().coinBalance(),
                copiedMetadata
        );
    }

    @Override
    public GuildMetaData build() {
        Objects.requireNonNull(creatorPlayerId, "creatorPlayerId");
        if (guildName == null || guildName.isBlank()) {
            throw new IllegalStateException("Guild name is required.");
        }
        if (tag == null || tag.isBlank()) {
            throw new IllegalStateException("Guild tag is required.");
        }

        Instant now = Instant.now();
        GuildId guildId = GuildId.of("guild-" + UUID.randomUUID());

        IGuildSettingsBuilder settingsBuilder = resolve(IGuildSettingsBuilder.class, new GuildSettingsBuilder());
        IPlayerGuildDataBuilder playerGuildDataBuilder = resolve(IPlayerGuildDataBuilder.class, new PlayerGuildDataBuilder());
        IGuildBankDataBuilder guildBankDataBuilder = resolve(IGuildBankDataBuilder.class, new GuildBankDataBuilder());
        IGuildMetaDataBuilder guildMetaDataBuilder = resolve(IGuildMetaDataBuilder.class, new GuildMetaDataBuilder());
        IGuildRankPermissionPolicy rankPermissionPolicy = resolve(IGuildRankPermissionPolicy.class, new GuildRankPermissionPolicy());

        GuildSettings settings = settingsBuilder
                .publicGuild(publicGuild)
                .inviteOnly(inviteOnly)
                .metadata(Map.of(
                        "guildName", guildName,
                        "guildTag", tag
                ))
                .build();

        PlayerGuildData owner = playerGuildDataBuilder
                .guildId(guildId)
                .universalPlayerId(creatorPlayerId)
                .rank(GuildRank.TIER_V)
                .roles(java.util.Set.of(GuildRole.REGENT))
                .joinedAt(now)
                .metadata(Map.of(
                        "owner", "true",
                        "creatorPlayerId", creatorPlayerId.toString()
                ))
                .build();

        Map<String, String> guildMetadata = new HashMap<>(metadata);
        guildMetadata.putIfAbsent("createdBy", creatorPlayerId.toString());
        guildMetadata.put("createdAt", Long.toString(now.toEpochMilli()));

        GuildBankData bank = guildBankDataBuilder
                .guildId(guildId)
                .coinBalance(startingCoins)
                .updatedAt(now)
                .metadata(Map.of(
                        "createdBy", creatorPlayerId.toString(),
                        "startingCoins", Long.toString(startingCoins)
                ))
                .build();

        return guildMetaDataBuilder
                .guildId(guildId)
                .name(guildName)
                .tag(tag)
                .motto(motto)
                .ownerPlayerId(creatorPlayerId)
                .state(GuildState.ACTIVE)
                .guildLevel(1)
                .createdAt(now)
                .updatedAt(now)
                .settings(settings)
                .bank(bank)
                .buildings(java.util.Set.of())
                .members(Map.of(creatorPlayerId, owner))
                .rankPermissions(rankPermissionPolicy.snapshot())
                .explicitPermissionExpansionAllowed(true)
                .metadata(guildMetadata)
                .build();
    }

    private static <T> T resolve(Class<T> type, T fallback) {
        try {
            return DependencyLoaderAccess.requireInstance(type);
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }
}
