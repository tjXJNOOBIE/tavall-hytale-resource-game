package com.tavall.resourcegame.services;

import com.tavall.resourcegame.domain.AgingState;
import com.tavall.resourcegame.domain.CitizenMetaData;
import com.tavall.resourcegame.domain.PopulationSummary;
import com.tavall.resourcegame.domain.TroopMetaData;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class PopulationMightTest {
    @Test
    void mightUsesTierCountsWhenPresent() {
        PopulationSummary summary = new PopulationSummary(
                12,
                7,
                CitizenMetaData.defaults(),
                new TroopMetaData(0.3, 0.3, 0.6, Map.of(1, 3, 2, 2, 4, 1)),
                AgingState.defaults(Instant.parse("2026-04-16T00:00:00Z"))
        );

        assertEquals(11, summary.might());
    }

    @Test
    void mightFallsBackToAggregateCountWhenTierCountsAreMissing() {
        PopulationSummary summary = new PopulationSummary(
                12,
                7,
                CitizenMetaData.defaults(),
                TroopMetaData.defaults(),
                AgingState.defaults(Instant.parse("2026-04-16T00:00:00Z"))
        );

        assertEquals(7, summary.might());
    }

    @Test
    void mightNeverDropsBelowZeroForMalformedAggregateCounts() {
        assertEquals(0, TroopMetaData.defaults().estimatedMight(-5));
    }
}
