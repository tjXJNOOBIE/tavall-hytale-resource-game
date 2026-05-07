package com.tavall.hytale.resourcegame.middleware.guild;

import java.util.Set;

public final class KingdomStateValidationHandler {
    public void requireState(GuildKingdom guildKingdom, Set<KingdomState> allowedStates) {
        if (!allowedStates.contains(guildKingdom.state())) {
            throw new KingdomOutOfStateException("Kingdom state " + guildKingdom.state() + " is not allowed for this action.");
        }
    }
}
