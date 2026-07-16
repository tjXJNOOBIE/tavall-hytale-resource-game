package org.tavall.control.guild;

public record GuildJobBuff(
        GuildJobTitle jobTitle,
        GuildJobDomain domain,
        double modifier
) {
}
