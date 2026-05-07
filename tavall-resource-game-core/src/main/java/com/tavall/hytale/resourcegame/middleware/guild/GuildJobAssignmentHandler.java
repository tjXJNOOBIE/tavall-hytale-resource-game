package com.tavall.hytale.resourcegame.middleware.guild;

import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;

import java.time.Instant;
import java.util.HashSet;

public final class GuildJobAssignmentHandler {
    private final GuildRepository guildRepository;

    public GuildJobAssignmentHandler(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    public GuildMemberProfile assignJobTitle(GuildId guildId, UniversalPlayerId universalPlayerId, GuildJobTitle jobTitle, Instant now) {
        GuildMemberProfile member = guildRepository.findMember(guildId, universalPlayerId)
                .orElseThrow(() -> new GuildValidationException("Guild member was not found."));
        HashSet<GuildJobTitle> jobs = new HashSet<>(member.jobTitles());
        jobs.add(jobTitle);
        return guildRepository.saveMember(member.withJobTitles(jobs));
    }
}
