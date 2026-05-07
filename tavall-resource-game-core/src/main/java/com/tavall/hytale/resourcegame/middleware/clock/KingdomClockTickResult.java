package com.tavall.hytale.resourcegame.middleware.clock;

import java.util.List;

public record KingdomClockTickResult(
        String kingdomId,
        KingdomClockState previousState,
        KingdomClockState currentState,
        boolean advanced,
        boolean phaseChanged,
        List<String> eventsEmitted
) {
    public KingdomClockTickResult {
        eventsEmitted = eventsEmitted == null ? List.of() : List.copyOf(eventsEmitted);
    }
}
