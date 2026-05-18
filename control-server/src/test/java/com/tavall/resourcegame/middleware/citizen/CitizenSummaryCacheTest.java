package org.tavall.control.citizen;

import org.tavall.control.domain.CitizenJobType;
import org.tavall.control.citizen.cache.CitizenSummaryCache;
import org.tavall.control.transport.JsonMapperProvider;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CitizenSummaryCacheTest {
    @Test
    void keepsSeparateMemoryAndSharedSummaryFlows() {
        CitizenSummaryCache cache = CitizenSummaryCache.openInMemory("test", new JsonMapperProvider().mapper());
        CitizenSummaryScope scope = CitizenSummaryScope.player(
                org.tavall.control.identity.UniversalPlayerId.of(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        );
        Instant now = Instant.parse("2026-05-16T12:00:00Z");
        CitizenSummaryBundle summary = new CitizenSummaryBundle(
                new CitizenPopulationSummary(
                        scope,
                        10,
                        8,
                        2,
                        1,
                        0,
                        0,
                        0,
                        0,
                        Map.of(CitizenAgeStage.ADULT, 8),
                        Map.of(CitizenJobType.GATHERER, 5),
                        Map.of(CitizenMoraleState.HIGH, 8),
                        Map.of(CitizenHealthState.HEALTHY, 10),
                        Map.of(CitizenHousingState.HOUSED, 10),
                        Map.of(CitizenNutritionState.FED, 10),
                        now
                ),
                new CitizenMedianSummary(scope, 1, 2, 3, 4, 5, 6, 7, 8, now),
                new CitizenProductivitySummary(scope, 9, 10, 11, 1, 1, 1, 1, now)
        );

        cache.primeShared(scope, summary);
        assertTrue(cache.readShared(scope).isPresent());
        assertFalse(cache.isSharedDirty(scope));

        cache.markSharedDirty(scope);
        assertTrue(cache.isSharedDirty(scope));
        assertFalse(cache.readShared(scope).isPresent());

        cache.primeMemory(scope, summary);
        assertEquals(8, cache.readMemory(scope).orElseThrow().populationSummary().activeCitizens());
        cache.invalidateMemory(scope);
        assertFalse(cache.readMemory(scope).isPresent());
    }
}

