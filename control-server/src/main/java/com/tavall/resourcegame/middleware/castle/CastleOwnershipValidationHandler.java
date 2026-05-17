package com.tavall.resourcegame.middleware.castle;

import com.tavall.resourcegame.middleware.identity.UniversalPlayerId;

public final class CastleOwnershipValidationHandler implements ICastleDomain {
    public void requireOwner(Castle castle, UniversalPlayerId actorPlayerId) {
        if (!castle.ownerPlayerId().equals(actorPlayerId)) {
            throw new CastleValidationException("Actor does not own castle.");
        }
    }
}
