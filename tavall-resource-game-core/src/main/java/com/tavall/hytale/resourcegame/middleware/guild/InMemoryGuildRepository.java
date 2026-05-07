package com.tavall.hytale.resourcegame.middleware.guild;

import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryGuildRepository implements GuildRepository {
    private final Map<GuildId, GuildKingdom> guildsById = new ConcurrentHashMap<>();
    private final Map<String, GuildMemberProfile> membersByGuildPlayer = new ConcurrentHashMap<>();

    @Override
    public GuildKingdom saveGuild(GuildKingdom guildKingdom) {
        guildsById.put(guildKingdom.guildId(), guildKingdom);
        return guildKingdom;
    }

    @Override
    public Optional<GuildKingdom> findGuild(GuildId guildId) {
        return Optional.ofNullable(guildsById.get(guildId));
    }

    @Override
    public GuildMemberProfile saveMember(GuildMemberProfile memberProfile) {
        membersByGuildPlayer.put(memberKey(memberProfile.guildId(), memberProfile.universalPlayerId()), memberProfile);
        return memberProfile;
    }

    @Override
    public Optional<GuildMemberProfile> findMember(GuildId guildId, UniversalPlayerId universalPlayerId) {
        return Optional.ofNullable(membersByGuildPlayer.get(memberKey(guildId, universalPlayerId)));
    }

    @Override
    public List<GuildMemberProfile> findMembers(GuildId guildId) {
        List<GuildMemberProfile> members = new ArrayList<>();
        for (GuildMemberProfile member : membersByGuildPlayer.values()) {
            if (member.guildId().equals(guildId)) {
                members.add(member);
            }
        }
        return List.copyOf(members);
    }

    private String memberKey(GuildId guildId, UniversalPlayerId universalPlayerId) {
        return guildId.value() + ":" + universalPlayerId.value();
    }
}
