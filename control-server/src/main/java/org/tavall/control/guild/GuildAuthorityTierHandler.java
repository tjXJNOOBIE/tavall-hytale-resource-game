package org.tavall.control.guild;

import org.tavall.control.identity.UniversalPlayerId;

public final class GuildAuthorityTierHandler implements GuildDomain {
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
