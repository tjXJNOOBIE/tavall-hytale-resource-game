package com.tavall.hytale.resourcegame.middleware.troop;

import com.tavall.hytale.resourcegame.middleware.guild.GuildId;

public final class GuildWarDeclarationHandler {
    public String declareWar(GuildId attackerGuildId, GuildId defenderGuildId) {
        if (attackerGuildId.equals(defenderGuildId)) {
            throw new TroopValidationException("Guild cannot declare war on itself.");
        }
        return attackerGuildId + "->" + defenderGuildId;
    }
}
