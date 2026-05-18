package org.tavall.control.world;

import org.tavall.control.domain.CastleLocationData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class CastlePromptLaneLayoutHandlerTest {
    @Test
    void createLayoutBuildsStableStandingPointInFrontOfCastle() {
        CastlePromptLaneLayoutHandler service = new CastlePromptLaneLayoutHandler();

        CastlePromptLaneLayout layout = service.createLayout(new CastleLocationData("overworld", 18.5, 72.9, 33.5));

        assertEquals(18.5, layout.origin().getX());
        assertEquals(71.0, layout.origin().getY());
        assertEquals(29.5, layout.origin().getZ());

        assertEquals(18.5, layout.alignmentPoint().getX());
        assertEquals(72.0, layout.alignmentPoint().getY());
        assertEquals(29.5, layout.alignmentPoint().getZ());
    }
}
