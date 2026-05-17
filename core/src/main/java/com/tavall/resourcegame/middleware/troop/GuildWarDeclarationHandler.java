package com.tavall.resourcegame.middleware.troop;

import com.tavall.resourcegame.middleware.guild.GuildId;

public final class GuildWarDeclarationHandler implements ITroopDomain {
    public String declareWar(GuildId attackerGuildId, GuildId defenderGuildId) {
        if (attackerGuildId.equals(defenderGuildId)) {
            throw new TroopValidationException("Guild cannot declare war on itself.");
        }
        return attackerGuildId + "->" + defenderGuildId;
    }
}
