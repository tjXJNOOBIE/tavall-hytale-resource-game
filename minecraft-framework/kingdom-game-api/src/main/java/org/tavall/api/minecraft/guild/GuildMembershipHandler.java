package org.tavall.api.minecraft.guild;

import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.dependency.annotations.DelegatesToInterface;
import org.tavall.dependency.composition.IDependencyBundleAccess;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@DelegatesToInterface(getLinkedInterface = IGuildMembershipHandler.class)
public final class GuildMembershipHandler implements IGuildMembershipHandler, IDependencyInjectableConcrete, IDependencyBundleAccess<IGuildDependencies> {
    public IGuildDependencies dependencies() {
        return getDependencies();
    }

    private IGuildStateStore guildStateStore() {
        return dependencies().guildStateStore();
    }

    private IGuildMetaDataBuilder guildMetaDataBuilder() {
        return dependencies().guildMetaDataBuilder();
    }

    private IPlayerGuildDataBuilder playerGuildDataBuilder() {
        return dependencies().playerGuildDataBuilder();
    }

    @Override
    public GuildMetaData invitePlayer(UUID actorPlayerId, UUID targetPlayerId) {
        Objects.requireNonNull(actorPlayerId, "actorPlayerId");
        Objects.requireNonNull(targetPlayerId, "targetPlayerId");

        GuildMetaData guild = guildStateStore().requireByMember(actorPlayerId);
        PlayerGuildData actor = guild.members().get(actorPlayerId);
        if (actor == null || !actor.hasPermission(GuildPermission.INVITE_PLAYER)) {
            throw new IllegalStateException("Actor cannot invite players.");
        }
        if (guildStateStore().findByMember(targetPlayerId).isPresent()) {
            throw new IllegalStateException("Target is already a member of a guild.");
        }

        Instant now = Instant.now();
        Map<String, String> metadata = new HashMap<>(guild.metadata());
        metadata.put(inviteMetadataKey(targetPlayerId), actorPlayerId + ":" + now.toEpochMilli());

        return guildStateStore().save(
                guildMetaDataBuilder()
                        .copyOf(guild)
                        .updatedAt(now)
                        .metadata(metadata)
                        .build()
        );
    }

    @Override
    public GuildMetaData kickPlayer(UUID actorPlayerId, UUID targetPlayerId) {
        Objects.requireNonNull(actorPlayerId, "actorPlayerId");
        Objects.requireNonNull(targetPlayerId, "targetPlayerId");

        GuildMetaData guild = guildStateStore().requireByMember(actorPlayerId);
        PlayerGuildData actor = guild.members().get(actorPlayerId);
        if (actor == null || !actor.hasPermission(GuildPermission.KICK_PLAYER)) {
            throw new IllegalStateException("Actor cannot kick players.");
        }
        if (guild.ownerPlayerId().equals(targetPlayerId)) {
            throw new IllegalStateException("Cannot kick the guild owner.");
        }

        PlayerGuildData target = guild.members().get(targetPlayerId);
        if (target == null) {
            throw new IllegalStateException("Target is not a guild member.");
        }

        Map<UUID, PlayerGuildData> members = new HashMap<>(guild.members());
        members.remove(targetPlayerId);

        Map<String, String> metadata = new HashMap<>(guild.metadata());
        metadata.remove(inviteMetadataKey(targetPlayerId));
        metadata.put("lastKickBy", actorPlayerId.toString());
        metadata.put("lastKickTarget", targetPlayerId.toString());

        Instant now = Instant.now();
        return guildStateStore().save(
                guildMetaDataBuilder()
                        .copyOf(guild)
                        .members(members)
                        .updatedAt(now)
                        .metadata(metadata)
                        .build()
        );
    }

    @Override
    public GuildMetaData promoteMember(UUID actorPlayerId, UUID targetPlayerId, GuildRank newRank) {
        Objects.requireNonNull(actorPlayerId, "actorPlayerId");
        Objects.requireNonNull(targetPlayerId, "targetPlayerId");
        Objects.requireNonNull(newRank, "newRank");

        GuildMetaData guild = guildStateStore().requireByMember(actorPlayerId);
        PlayerGuildData actor = guild.members().get(actorPlayerId);
        if (actor == null) {
            throw new IllegalStateException("Actor is not a guild member.");
        }

        PlayerGuildData target = guild.members().get(targetPlayerId);
        if (target == null) {
            throw new IllegalStateException("Target is not a guild member.");
        }
        if (newRank == target.rank()) {
            throw new IllegalStateException("Target is already at that rank.");
        }
        if (!newRank.atLeast(target.rank())) {
            throw new IllegalStateException("Promotions must move the member upward.");
        }

        if (newRank == GuildRank.TIER_V) {
            return transferOwnership(guild, actorPlayerId, targetPlayerId, actor, target);
        }
        if (!actor.hasPermission(GuildPermission.PROMOTE_MEMBER)) {
            throw new IllegalStateException("Actor cannot promote members.");
        }

        Map<UUID, PlayerGuildData> members = new HashMap<>(guild.members());
        members.put(
                targetPlayerId,
                playerGuildDataBuilder()
                        .copyOf(target)
                        .rank(newRank)
                        .build()
        );

        Map<String, String> metadata = new HashMap<>(guild.metadata());
        metadata.put("lastPromotionBy", actorPlayerId.toString());
        metadata.put("lastPromotionTarget", targetPlayerId.toString());
        metadata.put("lastPromotionRank", newRank.name());

        Instant now = Instant.now();
        return guildStateStore().save(
                guildMetaDataBuilder()
                        .copyOf(guild)
                        .members(members)
                        .updatedAt(now)
                        .metadata(metadata)
                        .build()
        );
    }

    private GuildMetaData transferOwnership(
            GuildMetaData guild,
            UUID actorPlayerId,
            UUID targetPlayerId,
            PlayerGuildData actor,
            PlayerGuildData target
    ) {
        if (!actor.hasPermission(GuildPermission.TRANSFER_OWNERSHIP)) {
            throw new IllegalStateException("Actor cannot transfer ownership.");
        }
        if (guild.ownerPlayerId().equals(targetPlayerId)) {
            throw new IllegalStateException("Target already owns the guild.");
        }

        Map<UUID, PlayerGuildData> members = new HashMap<>(guild.members());

        PlayerGuildData promotedTarget = playerGuildDataBuilder()
                .copyOf(target)
                .rank(GuildRank.TIER_V)
                .build();
        Set<GuildRole> promotedTargetRoles = new HashSet<>(promotedTarget.roles());
        promotedTargetRoles.add(GuildRole.REGENT);
        members.put(targetPlayerId, playerGuildDataBuilder().copyOf(promotedTarget).roles(promotedTargetRoles).build());

        PlayerGuildData currentOwner = members.get(guild.ownerPlayerId());
        if (currentOwner != null) {
            Set<GuildRole> ownerRoles = new HashSet<>(currentOwner.roles());
            ownerRoles.remove(GuildRole.REGENT);
            members.put(
                    guild.ownerPlayerId(),
                    playerGuildDataBuilder()
                            .copyOf(currentOwner)
                            .rank(GuildRank.TIER_IV)
                            .roles(ownerRoles)
                            .build()
            );
        }

        Map<String, String> metadata = new HashMap<>(guild.metadata());
        metadata.put("lastOwnershipTransferBy", actorPlayerId.toString());
        metadata.put("lastOwnershipTransferFrom", guild.ownerPlayerId().toString());
        metadata.put("lastOwnershipTransferTo", targetPlayerId.toString());

        Instant now = Instant.now();
        return guildStateStore().save(
                guildMetaDataBuilder()
                        .copyOf(guild)
                        .ownerPlayerId(targetPlayerId)
                        .members(members)
                        .updatedAt(now)
                        .metadata(metadata)
                        .build()
        );
    }

    @Override
    public GuildMetaData joinPublicGuild(UUID actorPlayerId, GuildId guildId) {
        Objects.requireNonNull(actorPlayerId, "actorPlayerId");
        Objects.requireNonNull(guildId, "guildId");

        if (guildStateStore().findByMember(actorPlayerId).isPresent()) {
            throw new IllegalStateException("Actor is already a member of a guild.");
        }

        GuildMetaData guild = guildStateStore().requireByGuildId(guildId);
        if (!guild.settings().publicGuild()) {
            throw new IllegalStateException("That guild is not public.");
        }

        Instant now = Instant.now();
        PlayerGuildData member = playerGuildDataBuilder()
                .guildId(guild.guildId())
                .universalPlayerId(actorPlayerId)
                .rank(GuildRank.TIER_I)
                .explicitPermissions(Set.of())
                .roles(Set.of())
                .joinedAt(now)
                .metadata(Map.of("joinedVia", "guild-join"))
                .build();

        Map<UUID, PlayerGuildData> members = new HashMap<>(guild.members());
        members.put(actorPlayerId, member);

        Map<String, String> metadata = new HashMap<>(guild.metadata());
        metadata.put("lastJoinBy", actorPlayerId.toString());
        metadata.put("lastJoinAt", Long.toString(now.toEpochMilli()));

        return guildStateStore().save(
                guildMetaDataBuilder()
                        .copyOf(guild)
                        .members(members)
                        .updatedAt(now)
                        .metadata(metadata)
                        .build()
        );
    }

    private static String inviteMetadataKey(UUID targetPlayerId) {
        return "invite:" + targetPlayerId;
    }
}

