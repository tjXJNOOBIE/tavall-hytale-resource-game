package org.tavall.control.guild;

import org.tavall.control.identity.UniversalPlayerId;

import java.time.Instant;
import java.util.Set;

public final class GuildMembershipHandler implements IGuildDomain {
    public GuildMembershipHandler() {
    }

    public GuildMembershipHandler(GuildRepository guildRepository) {
        registerGuildRepository(guildRepository);
    }

    public GuildMemberProfile addPlayerToGuild(GuildId guildId, UniversalPlayerId universalPlayerId, Instant now) {
        return getGuildRepository().saveMember(new GuildMemberProfile(
                guildId,
                universalPlayerId,
                GuildAuthorityTier.CITIZEN,
                Set.of(),
                Set.of(GuildJobTitle.MEMBER),
                now
        ));
    }
}
