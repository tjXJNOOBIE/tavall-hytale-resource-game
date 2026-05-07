package com.tavall.hytale.resourcegame.middleware.castle;

import com.tavall.hytale.resourcegame.middleware.common.CanonicalLocation;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class CastleMiddlewareIntegrationTest {
    @Test
    void castleCreationStoresCanonicalUniversalOwnerAndLocation() {
        InMemoryCastleRepository repository = new InMemoryCastleRepository();
        CastleCreationHandler creationHandler = new CastleCreationHandler(repository);
        UniversalPlayerId owner = UniversalPlayerId.random();

        Castle castle = creationHandler.createCastleForUniversalPlayer(owner, Optional.empty(), new CanonicalLocation("hytale-world", 12.0d, 71.0d, 9.0d));

        assertEquals(owner, castle.ownerPlayerId());
        assertEquals("hytale-world", castle.location().worldName());
        assertEquals("castle.basic.level_1", castle.globalAssetId().value());
        assertEquals(CastleState.ACTIVE, repository.findCastle(castle.castleId()).orElseThrow().state());
    }

    @Test
    void castleOwnershipValidationRejectsDifferentUniversalPlayer() {
        Castle castle = new CastleCreationHandler(new InMemoryCastleRepository())
                .createCastleForUniversalPlayer(UniversalPlayerId.random(), Optional.empty(), new CanonicalLocation("world", 1.0d, 2.0d, 3.0d));

        assertThrows(CastleValidationException.class, () -> new CastleOwnershipValidationHandler().requireOwner(castle, UniversalPlayerId.random()));
    }
}
