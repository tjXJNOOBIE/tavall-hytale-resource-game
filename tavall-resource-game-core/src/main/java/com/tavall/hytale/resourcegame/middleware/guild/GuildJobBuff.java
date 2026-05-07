package com.tavall.hytale.resourcegame.middleware.guild;

public record GuildJobBuff(
        GuildJobTitle jobTitle,
        GuildJobDomain domain,
        double modifier
) {
}
