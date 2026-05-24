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

@DelegatesToInterface(getLinkedInterface = IGuildRoleHandler.class)
public final class GuildRoleHandler implements IGuildRoleHandler, IDependencyInjectableConcrete, IDependencyBundleAccess<IGuildDependencies> {
    public IGuildDependencies dependencies() {
        return getDependencies();
    }

    private IGuildStateStore guildStateStore() {
        return dependencies().guildStateStore();
    }

    private IGuildRoleSlotPolicy guildRoleSlotPolicy() {
        return dependencies().guildRoleSlotPolicy();
    }

    private IGuildMetaDataBuilder guildMetaDataBuilder() {
        return dependencies().guildMetaDataBuilder();
    }

    private IPlayerGuildDataBuilder playerGuildDataBuilder() {
        return dependencies().playerGuildDataBuilder();
    }

    @Override
    public GuildMetaData assignRole(UUID actorPlayerId, UUID targetPlayerId, GuildRole role) {
        Objects.requireNonNull(actorPlayerId, "actorPlayerId");
        Objects.requireNonNull(targetPlayerId, "targetPlayerId");
        Objects.requireNonNull(role, "role");

        GuildMetaData guild = guildStateStore().requireByMember(actorPlayerId);
        PlayerGuildData actor = guild.members().get(actorPlayerId);
        if (actor == null || !actor.hasPermission(GuildPermission.MANAGE_GUILD_ROLES)) {
            throw new IllegalStateException("Actor cannot manage guild roles.");
        }

        PlayerGuildData target = guild.members().get(targetPlayerId);
        if (target == null) {
            throw new IllegalStateException("Target is not a guild member.");
        }
        if (!guild.unlockedRoles().contains(role)) {
            throw new IllegalStateException("Role is not unlocked yet.");
        }
        if (!role.isAssignableAt(target.rank())) {
            throw new IllegalStateException("Target rank is too low for this role.");
        }
        if (target.hasRole(role)) {
            return guild;
        }

        int roleSlots = guildRoleSlotPolicy().slotsForLevel(guild.guildLevel())
                + guild.buildings().stream().mapToInt(GuildBuildingType::additionalRoleSlots).sum();
        int assignedRoleCount = guild.members().values().stream().mapToInt(member -> member.roles().size()).sum();
        if (assignedRoleCount >= roleSlots) {
            throw new IllegalStateException("No guild role slots remain.");
        }

        Set<GuildRole> roles = new HashSet<>(target.roles());
        roles.add(role);

        Map<UUID, PlayerGuildData> members = new HashMap<>(guild.members());
        members.put(targetPlayerId, playerGuildDataBuilder().copyOf(target).roles(roles).build());

        Map<String, String> metadata = new HashMap<>(guild.metadata());
        metadata.put("lastRoleAssignedBy", actorPlayerId.toString());
        metadata.put("lastRoleAssignedTo", targetPlayerId.toString());
        metadata.put("lastRoleAssigned", role.name());

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
}

