package com.tavall.resourcegame.middleware.guild;

import com.tavall.resourcegame.middleware.identity.UniversalPlayerId;

public final class GuildAuthorityTierHandler implements IGuildDomain {
    public GuildAuthorityTierHandler() {
    }

    public GuildAuthorityTierHandler(GuildRepository guildRepository) {
        registerGuildRepository(guildRepository);
    }

    public GuildMemberProfile setAuthorityTier(GuildId guildId, UniversalPlayerId universalPlayerId, GuildAuthorityTier authorityTier) {
        GuildRepository guildRepository = getGuildRepository();
        GuildMemberProfile member = guildRepository.findMember(guildId, universalPlayerId)
                .orElseThrow(() -> new GuildValidationException("Guild member was not found."));
        return guildRepository.saveMember(member.withAuthorityTier(authorityTier));
    }
}
