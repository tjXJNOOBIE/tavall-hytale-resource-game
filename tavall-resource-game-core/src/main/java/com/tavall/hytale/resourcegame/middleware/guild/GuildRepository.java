package com.tavall.hytale.resourcegame.middleware.guild;

import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;

import java.util.List;
import java.util.Optional;

public interface GuildRepository {
    GuildKingdom saveGuild(GuildKingdom guildKingdom);

    Optional<GuildKingdom> findGuild(GuildId guildId);

    GuildMemberProfile saveMember(GuildMemberProfile memberProfile);

    Optional<GuildMemberProfile> findMember(GuildId guildId, UniversalPlayerId universalPlayerId);

    List<GuildMemberProfile> findMembers(GuildId guildId);
}
