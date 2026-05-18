package org.tavall.control.guild;

import org.tavall.control.identity.UniversalPlayerId;

import java.time.Instant;
import java.util.Objects;

public record GuildJobAssignment(
        GuildId guildId,
        UniversalPlayerId universalPlayerId,
        GuildJobTitle jobTitle,
        Instant assignedAt
) {
    public GuildJobAssignment {
        Objects.requireNonNull(guildId, "guildId");
        Objects.requireNonNull(universalPlayerId, "universalPlayerId");
        Objects.requireNonNull(jobTitle, "jobTitle");
        Objects.requireNonNull(assignedAt, "assignedAt");
    }
}
