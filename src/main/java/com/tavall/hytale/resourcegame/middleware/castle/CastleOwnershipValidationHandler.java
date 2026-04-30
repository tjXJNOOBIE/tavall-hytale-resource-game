package com.tavall.hytale.resourcegame.middleware.castle;

import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;

public final class CastleOwnershipValidationHandler {
    public void requireOwner(Castle castle, UniversalPlayerId actorPlayerId) {
        if (!castle.ownerPlayerId().equals(actorPlayerId)) {
            throw new CastleValidationException("Actor does not own castle.");
        }
    }
}
