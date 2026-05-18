package org.tavall.control.castle;

import org.tavall.control.identity.UniversalPlayerId;

public final class CastleOwnershipValidationHandler implements ICastleDomain {
    public void requireOwner(Castle castle, UniversalPlayerId actorPlayerId) {
        if (!castle.ownerPlayerId().equals(actorPlayerId)) {
            throw new CastleValidationException("Actor does not own castle.");
        }
    }
}
