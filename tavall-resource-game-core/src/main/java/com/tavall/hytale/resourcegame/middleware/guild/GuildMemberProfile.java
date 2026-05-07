package com.tavall.hytale.resourcegame.middleware.guild;

import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public record GuildMemberProfile(
        GuildId guildId,
        UniversalPlayerId universalPlayerId,
        GuildAuthorityTier authorityTier,
        Set<GuildPermission> explicitPermissions,
        Set<GuildJobTitle> jobTitles,
        Instant joinedAt
) {
    public GuildMemberProfile {
        Objects.requireNonNull(guildId, "guildId");
        Objects.requireNonNull(universalPlayerId, "universalPlayerId");
        Objects.requireNonNull(authorityTier, "authorityTier");
        explicitPermissions = explicitPermissions == null ? Set.of() : Set.copyOf(explicitPermissions);
        jobTitles = jobTitles == null ? Set.of() : Set.copyOf(jobTitles);
        Objects.requireNonNull(joinedAt, "joinedAt");
    }

    public GuildMemberProfile withAuthorityTier(GuildAuthorityTier authorityTier) {
        return new GuildMemberProfile(guildId, universalPlayerId, authorityTier, explicitPermissions, jobTitles, joinedAt);
    }

    public GuildMemberProfile withExplicitPermissions(Set<GuildPermission> permissions) {
        return new GuildMemberProfile(guildId, universalPlayerId, authorityTier, permissions, jobTitles, joinedAt);
    }

    public GuildMemberProfile withJobTitles(Set<GuildJobTitle> jobTitles) {
        return new GuildMemberProfile(guildId, universalPlayerId, authorityTier, explicitPermissions, jobTitles, joinedAt);
    }
}
