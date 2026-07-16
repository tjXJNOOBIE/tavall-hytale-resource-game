package org.tavall.control.world;

import org.tavall.control.domain.FocusedWorldTargetType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class FocusedWorldOverrideHandlerTest {
    @Test
    void peekAndConsumeNodeOverrideReturnSelectedTarget() {
        FocusedWorldOverrideHandler service = new FocusedWorldOverrideHandler();
        UUID playerId = UUID.fromString("11111111-2222-3333-4444-555555555555");
        UUID nodeId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        service.markNode(playerId, nodeId);

        var peeked = service.peek(playerId);
        assertTrue(peeked.isPresent());
        assertEquals(FocusedWorldTargetType.RESOURCE_NODE, peeked.get().type());
        assertEquals(nodeId, peeked.get().nodeId());

        var consumed = service.consume(playerId);
        assertTrue(consumed.isPresent());
        assertEquals(nodeId, consumed.get().nodeId());
        assertTrue(service.peek(playerId).isEmpty());
    }

    @Test
    void castleOverrideCanBeClearedExplicitly() {
        FocusedWorldOverrideHandler service = new FocusedWorldOverrideHandler();
        UUID playerId = UUID.fromString("11111111-2222-3333-4444-555555555555");

        service.markCastle(playerId);
        assertTrue(service.peek(playerId).isPresent());

        service.clear(playerId);
        assertTrue(service.consume(playerId).isEmpty());
    }
}
