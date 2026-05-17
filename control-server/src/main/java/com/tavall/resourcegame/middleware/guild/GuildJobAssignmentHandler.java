package com.tavall.resourcegame.middleware.guild;

import com.tavall.resourcegame.middleware.identity.UniversalPlayerId;

import java.time.Instant;
import java.util.HashSet;

public final class GuildJobAssignmentHandler implements IGuildDomain {
    public GuildJobAssignmentHandler() {
    }

    public GuildJobAssignmentHandler(GuildRepository guildRepository) {
        registerGuildRepository(guildRepository);
    }

    public GuildMemberProfile assignJobTitle(GuildId guildId, UniversalPlayerId universalPlayerId, GuildJobTitle jobTitle, Instant now) {
        GuildRepository guildRepository = getGuildRepository();
        GuildMemberProfile member = guildRepository.findMember(guildId, universalPlayerId)
                .orElseThrow(() -> new GuildValidationException("Guild member was not found."));
        HashSet<GuildJobTitle> jobs = new HashSet<>(member.jobTitles());
        jobs.add(jobTitle);
        return guildRepository.saveMember(member.withJobTitles(jobs));
    }
}
