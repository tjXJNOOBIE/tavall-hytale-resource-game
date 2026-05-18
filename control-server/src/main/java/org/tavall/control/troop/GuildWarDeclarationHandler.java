package org.tavall.control.troop;

import org.tavall.control.guild.GuildId;

public final class GuildWarDeclarationHandler implements TroopDomain {
    public String declareWar(GuildId attackerGuildId, GuildId defenderGuildId) {
        if (attackerGuildId.equals(defenderGuildId)) {
            throw new TroopValidationException("Guild cannot declare war on itself.");
        }
        return attackerGuildId + "->" + defenderGuildId;
    }
}
